package ai.saiy.android.command.hardware;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.OnOff;
import ai.saiy.android.permissions.PermissionHelper;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import ai.saiy.android.utils.UtilsWireless;

public class CommandHardware {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandHardware.class.getSimpleName();

    public static FlashlightController sFlashlightController;
    private static Camera sCamera;

    private final Outcome outcome = new Outcome();

    public @NonNull Outcome getResponse(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage, @NonNull ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        final long then = System.nanoTime();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
            if (ai.saiy.android.intent.ExecuteIntent.settingsIntent(context, IntentConstants.MANAGE_WRITE_SETTINGS)) {
                outcome.setUtterance(context.getString(R.string.permission_write_settings));
            } else {
                ai.saiy.android.applications.UtilsApplication.openApplicationSpecificSettings(context, context.getPackageName());
                outcome.setUtterance(context.getString(R.string.settings_missing, context.getString(R.string.content_modify_system_settings)));
            }
            outcome.setOutcome(Outcome.SUCCESS);
        } else if (!cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            final Pair<HardwareType, OnOff.Result> hardwarePair = new Hardware(supportedLanguage).sortHardware(context, voiceData);
            switch (hardwarePair.first) {
                case UNRESOLVED:
                    outcome.setUtterance(PersonalityResponse.getHardwareUnknownError(context, supportedLanguage));
                    outcome.setOutcome(Outcome.FAILURE);
                    break;
                case BLUETOOTH:
                    final String bluetooth = context.getString(R.string.bluetooth);
                    final BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (defaultAdapter == null) {
                        outcome.setOutcome(Outcome.FAILURE);
                        outcome.setUtterance(PersonalityResponse.getHardwareUnsupportedError(context, supportedLanguage));
                    } else if (hardwarePair.second == OnOff.Result.ON && defaultAdapter.isEnabled()) {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(bluetooth + XMLResultsHandler.SEP_SPACE + PersonalityResponse.getConnectionEnabled(context, supportedLanguage));
                    } else if (hardwarePair.second == OnOff.Result.OFF && !defaultAdapter.isEnabled()) {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(bluetooth + XMLResultsHandler.SEP_SPACE + PersonalityResponse.ConnectionDisabled(context, supportedLanguage));
                    } else {
                        @Nullable String errorMessage;
                        switch (hardwarePair.second) {
                            case ON:
                                errorMessage = setBluetoothEnabled(context, defaultAdapter, true, cr.getBundle());
                                if (errorMessage != null) {
                                    outcome.setOutcome(Outcome.FAILURE);
                                    outcome.setUtterance(errorMessage);
                                } else {
                                    outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, bluetooth, context.getString(R.string.on)));
                                }
                                break;
                            case OFF:
                                errorMessage = setBluetoothEnabled(context, defaultAdapter, false, cr.getBundle());
                                if (errorMessage != null) {
                                    outcome.setOutcome(Outcome.FAILURE);
                                    outcome.setUtterance(errorMessage);
                                } else {
                                    outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, bluetooth, context.getString(R.string.off)));
                                }
                                break;
                            case TOGGLE:
                            case UNRESOLVED:
                                if (defaultAdapter.isEnabled()) {
                                    errorMessage = setBluetoothEnabled(context, defaultAdapter, false, cr.getBundle());
                                    if (errorMessage != null) {
                                        outcome.setOutcome(Outcome.FAILURE);
                                        outcome.setUtterance(errorMessage);
                                    } else {
                                        outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, bluetooth, context.getString(R.string.off)));
                                    }
                                } else {
                                    errorMessage = setBluetoothEnabled(context, defaultAdapter, true, cr.getBundle());
                                    if (errorMessage != null) {
                                        outcome.setOutcome(Outcome.FAILURE);
                                        outcome.setUtterance(errorMessage);
                                    } else {
                                        outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, bluetooth, context.getString(R.string.on)));
                                    }
                                }
                                break;
                        }
                    }
                    break;
                case WIFI:
                    final String wifi = context.getString(R.string.wifi);
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (wifiManager == null) {
                        outcome.setOutcome(Outcome.FAILURE);
                        outcome.setUtterance(PersonalityResponse.getHardwareUnsupportedError(context, supportedLanguage));
                    } else if (hardwarePair.second == OnOff.Result.ON && wifiManager.isWifiEnabled()) {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(wifi + XMLResultsHandler.SEP_SPACE + PersonalityResponse.getConnectionEnabled(context, supportedLanguage));
                    } else if (hardwarePair.second == OnOff.Result.OFF && !wifiManager.isWifiEnabled()) {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(wifi + XMLResultsHandler.SEP_SPACE + PersonalityResponse.ConnectionDisabled(context, supportedLanguage));
                    } else {
                        @StringRes int errorMessage;
                        switch (hardwarePair.second) {
                            case ON:
                                errorMessage = setWifiEnabled(context, wifiManager, true);
                                if (errorMessage != 0) {
                                    outcome.setOutcome(Outcome.FAILURE);
                                    outcome.setUtterance(context.getString(errorMessage));
                                } else {
                                    outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, wifi, context.getString(R.string.on)));
                                }
                                break;
                            case OFF:
                                errorMessage = setWifiEnabled(context, wifiManager, false);
                                if (errorMessage != 0) {
                                    outcome.setOutcome(Outcome.FAILURE);
                                    outcome.setUtterance(context.getString(errorMessage));
                                } else {
                                    outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, wifi, context.getString(R.string.off)));
                                }
                                break;
                            case TOGGLE:
                            case UNRESOLVED:
                                if (wifiManager.isWifiEnabled()) {
                                    errorMessage = setWifiEnabled(context, wifiManager, false);
                                    if (errorMessage != 0) {
                                        outcome.setOutcome(Outcome.FAILURE);
                                        outcome.setUtterance(context.getString(errorMessage));
                                    } else {
                                        outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, wifi, context.getString(R.string.off)));
                                    }
                                } else {
                                    errorMessage = setWifiEnabled(context, wifiManager, true);
                                    if (errorMessage != 0) {
                                        outcome.setOutcome(Outcome.FAILURE);
                                        outcome.setUtterance(context.getString(errorMessage));
                                    } else {
                                        outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, wifi, context.getString(R.string.on)));
                                    }
                                }
                                break;
                        }
                    }
                    break;
                case DATA:
                    final String theDataConnection = context.getString(R.string.the) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.data) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.connection);
                    final MobileDataHelper mobileDataHelper = new MobileDataHelper();
                    if (!mobileDataHelper.hasTelephonyFeature(context)) {
                        outcome.setOutcome(Outcome.FAILURE);
                        outcome.setUtterance(PersonalityResponse.getHardwareUnsupportedError(context, supportedLanguage));
                    } else if (hardwarePair.second == OnOff.Result.ON && mobileDataHelper.isEnabled(context)) {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(theDataConnection + XMLResultsHandler.SEP_SPACE + PersonalityResponse.getConnectionEnabled(context, supportedLanguage));
                    } else if (hardwarePair.second == OnOff.Result.OFF && !mobileDataHelper.isEnabled(context)) {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(theDataConnection + XMLResultsHandler.SEP_SPACE + PersonalityResponse.ConnectionDisabled(context, supportedLanguage));
                    } else {
                        boolean isFailed = false;
                        switch (hardwarePair.second) {
                            case ON:
                                if (mobileDataHelper.enable(context)) {
                                    outcome.setOutcome(Outcome.SUCCESS);
                                    outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, theDataConnection, context.getString(R.string.on)));
                                } else {
                                    isFailed = true;
                                }
                                break;
                            case OFF:
                                if (mobileDataHelper.disable(context)) {
                                    outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, theDataConnection, context.getString(R.string.off)));
                                } else {
                                    isFailed = true;
                                }
                                break;
                            case TOGGLE:
                            case UNRESOLVED:
                                if (mobileDataHelper.isEnabled(context)) {
                                    if (mobileDataHelper.disable(context)) {
                                        outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, theDataConnection, context.getString(R.string.off)));
                                    } else {
                                        isFailed = true;
                                    }
                                } else {
                                    if (mobileDataHelper.enable(context)) {
                                        outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, theDataConnection, context.getString(R.string.on)));
                                    } else {
                                        isFailed = true;
                                    }
                                }
                                break;
                        }
                        if (isFailed) {
                            outcome.setOutcome(Outcome.FAILURE);
                            outcome.setUtterance(PersonalityResponse.getHardwareUnsupportedError(context, supportedLanguage));
                        }
                    }
                    break;
                case AEROPLANE:
                    final String flightMode = context.getString(R.string.flight) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.mode);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        outcome.setOutcome(Outcome.FAILURE);
                        outcome.setUtterance(PersonalityResponse.getHardwareUnsupportedError(context, supportedLanguage));
                    } else {
                        final boolean isEnabled;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            isEnabled = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
                        } else {
                            isEnabled = Settings.System.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
                        }
                        if (isEnabled && hardwarePair.second == OnOff.Result.ON) {
                            outcome.setOutcome(Outcome.SUCCESS);
                            outcome.setUtterance(flightMode + XMLResultsHandler.SEP_SPACE + PersonalityResponse.getConnectionEnabled(context, supportedLanguage));
                        } else if (!isEnabled && hardwarePair.second == OnOff.Result.OFF) {
                            outcome.setOutcome(Outcome.SUCCESS);
                            outcome.setUtterance(flightMode + XMLResultsHandler.SEP_SPACE + PersonalityResponse.ConnectionDisabled(context, supportedLanguage));
                        } else {
                            boolean enabled = false;
                            switch (hardwarePair.second) {
                                case ON:
                                    enabled = true;
                                    break;
                                case OFF:
                                    enabled = false;
                                    break;
                                case TOGGLE:
                                case UNRESOLVED:
                                    enabled = !isEnabled;
                                    break;
                            }

                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    Settings.Global.putInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, enabled ? 1 : 0);
                                } else {
                                    Settings.System.putInt(context.getContentResolver(), "airplane_mode_on", enabled ? 1 : 0);
                                }
                                final Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                                intent.putExtra("state", enabled);
                                context.sendBroadcast(intent);
                                outcome.setOutcome(Outcome.SUCCESS);
                                if (enabled) {
                                    outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, flightMode, context.getString(R.string.on)));
                                } else {
                                    outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, flightMode, context.getString(R.string.off)));
                                }
                            } catch (Exception e) {
                                if (DEBUG) {
                                    MyLog.e(CLS_NAME, "toggleAirplaneMode: failed");
                                    e.printStackTrace();
                                }
                                outcome.setOutcome(Outcome.FAILURE);
                                outcome.setUtterance(PersonalityResponse.getHardwareUnsupportedError(context, supportedLanguage));
                            }
                        }
                    }
                    break;
                case GPS:
                    final String gps = context.getString(R.string.gps);
                    final LocationManager locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager == null || !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
                        outcome.setOutcome(Outcome.FAILURE);
                        outcome.setUtterance(PersonalityResponse.getHardwareUnsupportedError(context, supportedLanguage));
                    }else if (hardwarePair.second == OnOff.Result.ON && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(gps + XMLResultsHandler.SEP_SPACE + PersonalityResponse.getConnectionEnabled(context, supportedLanguage));
                    } else if (hardwarePair.second == OnOff.Result.OFF && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(gps + XMLResultsHandler.SEP_SPACE + PersonalityResponse.ConnectionDisabled(context, supportedLanguage));
                    } else {
                        switch (hardwarePair.second) {
                            case ON:
                                locationSettings(context);
                                outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, gps, context.getString(R.string.on)));
                                break;
                            case OFF:
                                locationSettings(context);
                                outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, gps, context.getString(R.string.off)));
                                break;
                            case TOGGLE:
                            case UNRESOLVED:
                                locationSettings(context);
                                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, gps, context.getString(R.string.off)));
                                } else {
                                    outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, gps, context.getString(R.string.on)));
                                }
                                break;
                        }
                    }
                    break;
                case NFC:
                    outcome.setOutcome(Outcome.FAILURE);
                    outcome.setUtterance(PersonalityResponse.getHardwareUnsupportedError(context, supportedLanguage));
                    break;
                case HOTSPOT:
                    final String theHotspot = context.getString(R.string.the) + XMLResultsHandler.SEP_SPACE + context.getString(R.string.hotspot);
                    WifiManager wifiManager2 = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (wifiManager2 == null) {
                        outcome.setOutcome(Outcome.FAILURE);
                        outcome.setUtterance(PersonalityResponse.getHardwareUnsupportedError(context, supportedLanguage));
                    } else {
                        final WifiManagerHelper wifiManagerHelper = new WifiManagerHelper();
                        final boolean isEnabled = wifiManagerHelper.isEnabled(wifiManager2);
                        if (isEnabled && hardwarePair.second == OnOff.Result.ON) {
                            outcome.setOutcome(Outcome.SUCCESS);
                            outcome.setUtterance(theHotspot + XMLResultsHandler.SEP_SPACE + PersonalityResponse.getConnectionEnabled(context, supportedLanguage));
                        } else if (!isEnabled && hardwarePair.second == OnOff.Result.OFF) {
                            outcome.setOutcome(Outcome.SUCCESS);
                            outcome.setUtterance(theHotspot + XMLResultsHandler.SEP_SPACE + PersonalityResponse.ConnectionDisabled(context, supportedLanguage));
                        } else {
                            boolean enabled;
                            switch (hardwarePair.second) {
                                case ON:
                                    enabled = true;
                                    break;
                                case OFF:
                                    enabled = false;
                                    break;
                                case TOGGLE:
                                case UNRESOLVED:
                                    enabled = !isEnabled;
                                    break;
                                default:
                                    enabled = false;
                                    break;
                            }

                            outcome.setOutcome(Outcome.SUCCESS);
                            if (enabled) {
                                wifiManagerHelper.enable(context, wifiManager2);
                                outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, theHotspot, context.getString(R.string.on)));
                            } else {
                                wifiManagerHelper.disable(context, wifiManager2);
                                outcome.setUtterance(PersonalityResponse.getHardwareToggle(context, supportedLanguage, theHotspot, context.getString(R.string.off)));
                            }
                        }
                    }
                    break;
                case FLASHLIGHT:
                    if (!ai.saiy.android.permissions.PermissionHelper.checkCameraPermissions(context, cr.getBundle())) {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(SaiyRequestParams.SILENCE);
                    } else {
                        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                            outcome.setOutcome(Outcome.FAILURE);
                            outcome.setUtterance(PersonalityResponse.getHardwareUnsupportedError(context, supportedLanguage));
                        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            try {
                                if (sCamera == null) {
                                    sCamera = Camera.open();
                                }
                                Camera.Parameters parameters;
                                try {
                                    parameters = sCamera.getParameters();
                                } catch (Exception e) {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "Exception nested");
                                        e.printStackTrace();
                                    }
                                    try {
                                        sCamera.stopPreview();
                                        sCamera.release();
                                        sCamera = null;
                                        parameters = null;
                                    } catch (Exception exception) {
                                        if (DEBUG) {
                                            MyLog.w(CLS_NAME, "Exception super nested");
                                            e.printStackTrace();
                                        }
                                        sCamera = null;
                                        parameters = null;
                                    }
                                }
                                if (sCamera == null) {
                                    sCamera = Camera.open();
                                    parameters = sCamera.getParameters();
                                }
                                String flashMode = parameters.getFlashMode();
                                if (!UtilsString.notNaked(flashMode)) {
                                    outcome.setOutcome(Outcome.FAILURE);
                                    outcome.setUtterance(PersonalityResponse.getHardwareUnsupportedError(context, supportedLanguage));
                                } else if (TextUtils.equals(flashMode, Camera.Parameters.FLASH_MODE_TORCH)) {
                                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                                    sCamera.setParameters(parameters);
                                    sCamera.stopPreview();
                                    sCamera.release();
                                    sCamera = null;
                                    outcome.setOutcome(Outcome.SUCCESS);
                                    outcome.setUtterance(context.getString(R.string.okay));
                                } else {
                                    if (ai.saiy.android.utils.SPH.getTorchFix(context)) {
                                        sCamera.setPreviewTexture(new SurfaceTexture(0));
                                    }
                                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                                    sCamera.setParameters(parameters);
                                    sCamera.startPreview();
                                    outcome.setOutcome(Outcome.SUCCESS);
                                    outcome.setUtterance(context.getString(R.string.okay));
                                }
                            } catch (NullPointerException e) {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "NullPointerException");
                                    e.printStackTrace();
                                }
                                outcome.setOutcome(Outcome.FAILURE);
                                outcome.setUtterance(PersonalityResponse.getHardwareCameraError(context, supportedLanguage));
                                sCamera = null;
                            } catch (Exception e) {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "Exception");
                                    e.printStackTrace();
                                }
                                outcome.setOutcome(Outcome.FAILURE);
                                outcome.setUtterance(PersonalityResponse.getHardwareCameraError(context, supportedLanguage));
                                sCamera = null;
                            }
                        } else {
                            if (sFlashlightController == null) {
                                sFlashlightController = new FlashlightController(context);
                            }
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "flashlightController isAvailable: " + sFlashlightController.isAvailable());
                                MyLog.i(CLS_NAME, "flashlightController enabled: " + sFlashlightController.isEnabled());
                            }
                            sFlashlightController.setFlashlight(!sFlashlightController.isEnabled());
                            outcome.setOutcome(Outcome.SUCCESS);
                            outcome.setUtterance(context.getString(R.string.okay));
                        }
                    }
                    break;
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "isResolved: true");
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }

    @SuppressLint("MissingPermission")
    private @Nullable String setBluetoothEnabled(final Context ctx, BluetoothAdapter defaultAdapter, boolean enabled, @Nullable Bundle bundle) {
        if (enabled) {
            // Show message if Bluetooth is not allowed in airplane mode
            if (!UtilsWireless.isRadioAllowed(ctx.getApplicationContext(), (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)? Settings.Global.RADIO_BLUETOOTH : Settings.System.RADIO_BLUETOOTH)) {
                return ctx.getString(R.string.wifi_in_airplane_mode);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !PermissionHelper.checkBluetoothPermissions(ctx, bundle)) {
            return SaiyRequestParams.SILENCE;
        }
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(enableBtIntent);
            } else {
                defaultAdapter.enable();
            }
        } else {
            final Intent disableBtIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            disableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(disableBtIntent);
            defaultAdapter.disable(); // User need manually disable it
        }

        //https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#ACTION_STATE_CHANGED
        return null;
    }

    private @StringRes int setWifiEnabled(final Context ctx, WifiManager wifiManager, boolean enabled) {
        if (enabled) {
            // Show message if Wi-Fi is not allowed in airplane mode
            if (!UtilsWireless.isRadioAllowed(ctx.getApplicationContext(), (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)? Settings.Global.RADIO_WIFI : Settings.System.RADIO_WIFI)) {
                return R.string.wifi_in_airplane_mode;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            final Intent panelIntent = new Intent(android.provider.Settings.Panel.ACTION_WIFI);
            panelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(panelIntent);
        } else {
            if (!wifiManager.setWifiEnabled(enabled)) {
                return R.string.wifi_error;
            }
        }
        return 0;
    }

    private void locationSettings(final Context ctx) {
        final Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        locationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(locationIntent);
    }
}
