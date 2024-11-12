/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.permissions;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.PermissionChecker;

import ai.saiy.android.ui.activity.ActivityPermissionDialog;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsBundle;
import ai.saiy.android.utils.UtilsString;

/**
 * Created by benrandall76@gmail.com on 12/04/2016.
 */
public class PermissionHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = PermissionHelper.class.getSimpleName();

    public static final String REQUESTED_PERMISSION = "requested_permission";
    public static final String REQUESTED_PERMISSION_ID = "requested_permission_id";

    public static final int REQUEST_UNKNOWN = 0;
    public static final int REQUEST_AUDIO = 1;
    public static final int REQUEST_FILE = 2;
    public static final int REQUEST_GROUP_CONTACTS = 3;
    public static final int REQUEST_GROUP_TELEPHONY = 4;
    public static final int REQUEST_CAMERA = 5;
    public static final int REQUEST_LOCATION = 6;
    public static final int REQUEST_CALENDAR = 7;
    public static final int REQUEST_SMS_READ = 8;
    public static final int REQUEST_SMS_SEND = 9;
    public static final int REQUEST_PHONE_STATE = 10;
    public static final int REQUEST_READ_PHONE_STATE = 11;
    public static final int REQUEST_BACKGROUND_LOCATION = 12;
    public static final int REQUEST_BLUETOOTH_CONNECT = 13;

    /**
     * Check if the calling application has the correct permission to control Saiy.
     *
     * @param ctx the application context
     * @return true if the permission has been granted.
     */
    public static boolean checkSaiyRemotePermission(@NonNull final Context ctx) {

        switch (ctx.checkCallingPermission(Constants.PERMISSION_CONTROL_SAIY)) {

            case PackageManager.PERMISSION_GRANTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkSaiyRemotePermission: PERMISSION_GRANTED");
                }
                return true;
            case PackageManager.PERMISSION_DENIED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkSaiyRemotePermission: PERMISSION_DENIED");
                }
            default:
                return false;
        }
    }

    private static int hasSelfPermission(Context context, String permission) {
        try {
            return PermissionChecker.checkSelfPermission(context, permission);
        } catch (RuntimeException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "hasSelfPermission: RuntimeException");
                e.printStackTrace();
            }
            return PermissionChecker.PERMISSION_DENIED;
        }
    }

    public static Pair<Boolean, Boolean> checkTutorialPermissions(Context context) {
        boolean hasAudioPermission;
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkTutorialPermissions");
        }
        switch (hasSelfPermission(context, android.Manifest.permission.RECORD_AUDIO)) {
            case PermissionChecker.PERMISSION_GRANTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkTutorialPermissions: PERMISSION_GRANTED");
                }
                hasAudioPermission = true;
                break;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "checkTutorialPermissions: PERMISSION_DENIED");
                }
                hasAudioPermission = false;
                break;
        }
        return new Pair<>(hasAudioPermission, Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context));
    }

    /**
     * Check if the calling application has the correct permission to control Saiy.
     *
     * @param ctx        the application context
     * @param callingUid of the remote request
     * @return true if the permission has been granted.
     */
    public static boolean checkSaiyPermission(@NonNull final Context ctx, final int callingUid) {

        final String packageName = ctx.getPackageManager().getNameForUid(callingUid);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkSaiyPermission: " + packageName);
        }

        if (UtilsString.notNaked(packageName) && callingUid > 0) {

            if (!packageName.matches(ctx.getPackageName())) {

                switch (ctx.checkCallingPermission(Constants.PERMISSION_CONTROL_SAIY)) {

                    case PackageManager.PERMISSION_GRANTED:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "checkSaiyPermission: PERMISSION_GRANTED");
                        }
                        return true;
                    case PackageManager.PERMISSION_DENIED:
                    default:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "checkSaiyPermission: PERMISSION_DENIED");
                        }
                        return false;
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkSaiyPermission: self");
                }
                return true;
            }
        } else {
            MyLog.e(CLS_NAME, ctx.getString(ai.saiy.android.api.R.string.error_package_name_null));
        }

        return false;
    }

    /**
     * Check if the user has granted microphone permissions
     *
     * @param ctx the application context
     * @return true if the permissions have been granted. False if they have been denied or are
     * required to be requested.
     */
    public static boolean checkAudioPermissions(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkAudioPermissions");
        }

        switch (hasSelfPermission(ctx, android.Manifest.permission.RECORD_AUDIO)) {

            case PermissionChecker.PERMISSION_GRANTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkAudioPermissions: PERMISSION_GRANTED");
                }
                return true;
            case PermissionChecker.PERMISSION_DENIED:
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "checkAudioPermissions: PERMISSION_DENIED");
                }

                final Intent intent = new Intent(ctx, ActivityPermissionDialog.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                final Bundle bundle = new Bundle();
                bundle.putStringArray(REQUESTED_PERMISSION, new String[]{android.Manifest.permission.RECORD_AUDIO});
                bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_AUDIO);

                intent.putExtras(bundle);

                ctx.startActivity(intent);
                return false;
        }
    }

    public static boolean checkTelephonyGroupPermissions(Context context, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkTelephonyGroupPermissions");
        }
        if (hasSelfPermission(context, Manifest.permission.CALL_PHONE) == PermissionChecker.PERMISSION_GRANTED) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkTelephonyGroupPermissions: PERMISSION_GRANTED");
            }
            return true;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "checkTelephonyGroupPermissions: PERMISSION_DENIED");
        }
        final Intent intent = new Intent(context, ActivityPermissionDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!UtilsBundle.notNaked(bundle)) {
            bundle = new Bundle();
        }
        bundle.putStringArray(REQUESTED_PERMISSION, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG});
        bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_GROUP_TELEPHONY);
        intent.putExtras(bundle);
        context.startActivity(intent);
        return false;
    }

    public static boolean checkPhoneStatePermissionsNR(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkPhoneStatePermissionsNR");
        }
        if (hasSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PermissionChecker.PERMISSION_GRANTED) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkPhoneStatePermissionsNR: PERMISSION_GRANTED");
            }
            return true;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "checkPhoneStatePermissionsNR: PERMISSION_DENIED");
        }
        return false;
    }

    public static boolean checkAnswerCallsPermissionsNR(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkAnswerCallsPermissionsNR");
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return true;
        }
        if (hasSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PermissionChecker.PERMISSION_GRANTED) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkAnswerCallsPermissionsNR: PERMISSION_GRANTED");
            }
            return true;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "checkAnswerCallsPermissionsNR: PERMISSION_DENIED");
        }
        return false;
    }

    public static boolean checkCameraPermissions(Context context, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkCameraPermissions");
        }
        if (hasSelfPermission(context, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkCameraPermissions: PERMISSION_GRANTED");
            }
            return true;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "checkCameraPermissions: PERMISSION_DENIED");
        }
        final Intent intent = new Intent(context, ActivityPermissionDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!UtilsBundle.notNaked(bundle)) {
            bundle = new Bundle();
        }
        bundle.putStringArray(REQUESTED_PERMISSION, new String[]{Manifest.permission.CAMERA});
        bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_CAMERA);
        intent.putExtras(bundle);
        context.startActivity(intent);
        return false;
    }

    public static boolean checkLocationPermissions(Context context, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkLocationPermissions");
        }
        if (hasSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED ||
                hasSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && hasSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PermissionChecker.PERMISSION_GRANTED) {
                final Intent intent = new Intent(context, ActivityPermissionDialog.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (!UtilsBundle.notNaked(bundle)) {
                    bundle = new Bundle();
                }
                bundle.putStringArray(REQUESTED_PERMISSION, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION});
                bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_BACKGROUND_LOCATION);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkLocationPermissions: PERMISSION_GRANTED");
            }
            return true;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "checkLocationPermissions: PERMISSION_DENIED");
        }
        final Intent intent = new Intent(context, ActivityPermissionDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!UtilsBundle.notNaked(bundle)) {
            bundle = new Bundle();
        }
        bundle.putStringArray(REQUESTED_PERMISSION, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_LOCATION);
        intent.putExtras(bundle);
        context.startActivity(intent);
        return false;
    }

    public static boolean checkCalendarPermissions(Context context, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkCalendarPermissions");
        }
        if (hasSelfPermission(context, Manifest.permission.READ_CALENDAR) == PermissionChecker.PERMISSION_GRANTED) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkCalendarPermissions: PERMISSION_GRANTED");
            }
            return true;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "checkCalendarPermissions: PERMISSION_DENIED");
        }
        final Intent intent = new Intent(context, ActivityPermissionDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!UtilsBundle.notNaked(bundle)) {
            bundle = new Bundle();
        }
        bundle.putStringArray(REQUESTED_PERMISSION, new String[]{Manifest.permission.READ_CALENDAR});
        bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_CALENDAR);
        intent.putExtras(bundle);
        context.startActivity(intent);
        return false;
    }

    public static boolean checkPhoneStatePermissions(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkPhoneStatePermissions");
        }
        if (checkPhoneStatePermissionsNR(context) && checkAnswerCallsPermissionsNR(context)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkPhoneStatePermissions: PERMISSION_GRANTED");
            }
            return true;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "checkPhoneStatePermissions: PERMISSION_DENIED");
        }
        Intent intent = new Intent(context, ActivityPermissionDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bundle.putStringArray(REQUESTED_PERMISSION, new String[]{android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.ANSWER_PHONE_CALLS});
        } else {
            bundle.putStringArray(REQUESTED_PERMISSION, new String[]{android.Manifest.permission.READ_PHONE_STATE});
        }
        bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_PHONE_STATE);
        intent.putExtras(bundle);
        context.startActivity(intent);
        return false;
    }

    public static boolean checkSMSReadPermissions(Context context, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkSMSReadPermissions");
        }
        switch (hasSelfPermission(context, android.Manifest.permission.READ_SMS)) {
            case PermissionChecker.PERMISSION_GRANTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkSMSReadPermissions: PERMISSION_GRANTED");
                }
                return true;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "checkSMSReadPermissions: PERMISSION_DENIED");
                }
                Intent intent = new Intent(context, ActivityPermissionDialog.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (!UtilsBundle.notNaked(bundle)) {
                    bundle = new Bundle();
                }
                bundle.putStringArray(REQUESTED_PERMISSION, new String[]{android.Manifest.permission.READ_SMS});
                bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_SMS_READ);
                intent.putExtras(bundle);
                context.startActivity(intent);
                return false;
        }
    }

    public static boolean checkAnnounceCallerPermissionsNR(Context context) {
        return checkPhoneStatePermissionsNR(context) && checkAnswerCallsPermissionsNR(context) && checkNotificationPolicyPermission(context) && checkContactGroupPermissionsNR(context);
    }

    public static boolean checkSMSSendPermissions(Context context, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkSMSSendPermissions");
        }
        if (hasSelfPermission(context, Manifest.permission.SEND_SMS) == PermissionChecker.PERMISSION_GRANTED) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkSMSSendPermissions: PERMISSION_GRANTED");
            }
            return true;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "checkSMSSendPermissions: PERMISSION_DENIED");
        }
        final Intent intent = new Intent(context, ActivityPermissionDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!UtilsBundle.notNaked(bundle)) {
            bundle = new Bundle();
        }
        bundle.putStringArray(REQUESTED_PERMISSION, new String[]{Manifest.permission.SEND_SMS});
        bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_SMS_SEND);
        intent.putExtras(bundle);
        context.startActivity(intent);
        return false;
    }

    public static boolean checkReadCallerPermissions(Context context) {
        if (!checkPhoneStatePermissionsNR(context)) {
            checkPhoneStatePermissions(context);
            return false;
        }
        if (checkContactGroupPermissionsNR(context)) {
            return true;
        }
        checkContactGroupPermissions(context, null);
        return false;
    }

    /**
     * Check to see if we have the read phone state permission
     *
     * @return true if the permission has been granted
     */
    public static boolean checkReadPhoneStatePermission(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkReadPhoneStatePermission");
        }
        if (checkPhoneStatePermissionsNR(ctx)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkReadPhoneStatePermission: PERMISSION_GRANTED");
            }
            return true;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "checkReadPhoneStatePermission: PERMISSION_DENIED");
        }
        final Intent intent = new Intent(ctx, ActivityPermissionDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final Bundle bundle = new Bundle();
        bundle.putStringArray(REQUESTED_PERMISSION, new String[]{android.Manifest.permission.READ_PHONE_STATE});
        bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_READ_PHONE_STATE);
        intent.putExtras(bundle);
        ctx.startActivity(intent);
        return false;
    }

    /**
     * Check if the user has granted write files permissions
     *
     * @param ctx the application context
     * @return true if the permissions have been granted. False if they have been denied or are
     * required to be requested.
     */
    public static boolean checkFilePermissions(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkFilePermissions");
        }

        switch (hasSelfPermission(ctx, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            case PermissionChecker.PERMISSION_GRANTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkFilePermissions: PERMISSION_GRANTED");
                }
                return true;
            case PermissionChecker.PERMISSION_DENIED:
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "checkFilePermissions: PERMISSION_DENIED");
                }

                final Intent intent = new Intent(ctx, ActivityPermissionDialog.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                final Bundle bundle = new Bundle();
                bundle.putStringArray(REQUESTED_PERMISSION, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE});
                bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_FILE);

                intent.putExtras(bundle);

                ctx.startActivity(intent);
                return false;
        }
    }

    public static boolean checkFilePermissionsNR(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkFilePermissionsNR");
        }

        switch (hasSelfPermission(ctx, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            case PermissionChecker.PERMISSION_GRANTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkFilePermissionsNR: PERMISSION_GRANTED");
                }
                return true;
            case PermissionChecker.PERMISSION_DENIED:
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "checkFilePermissionsNR: PERMISSION_DENIED");
                }
                return false;
        }
    }

    public static boolean checkContactGroupPermissionsNR(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkContactGroupPermissionsNR");
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            switch (hasSelfPermission(ctx, android.Manifest.permission.GET_ACCOUNTS)) {
                case PermissionChecker.PERMISSION_GRANTED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "checkContactGroupPermissionsNR: PERMISSION_GRANTED");
                    }
                    return true;
                case PermissionChecker.PERMISSION_DENIED:
                default:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "checkContactGroupPermissionsNR: PERMISSION_DENIED");
                    }
                    return false;
            }
        }
        if (!checkReadContactPermissionNR(ctx) || !checkAccountsPermissionNR(ctx) || !checkWriteContactsPermissionNR(ctx)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "checkContactGroupPermissionsNR: PERMISSION_DENIED");
            }
            return false;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkContactGroupPermissionsNR: PERMISSION_GRANTED");
        }
        return true;
    }

    public static boolean checkReadContactPermissionNR(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkReadContactPermissionNR");
        }
        if (hasSelfPermission(context, Manifest.permission.READ_CONTACTS) == PermissionChecker.PERMISSION_GRANTED) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkReadContactPermissionNR: PERMISSION_GRANTED");
            }
            return true;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "checkReadContactPermissionNR: PERMISSION_DENIED");
        }
        return false;
    }

    public static boolean checkAccountsPermissionNR(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkAccountsPermissionNR");
        }
        if (hasSelfPermission(context, Manifest.permission.GET_ACCOUNTS) == PermissionChecker.PERMISSION_GRANTED) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkAccountsPermissionNR: PERMISSION_GRANTED");
            }
            return true;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "checkAccountsPermissionNR: PERMISSION_DENIED");
        }
        return false;
    }

    public static boolean checkWriteContactsPermissionNR(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkWriteContactsPermissionNR");
        }
        if (hasSelfPermission(context, Manifest.permission.WRITE_CONTACTS) == PermissionChecker.PERMISSION_GRANTED) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "checkWriteContactsPermissionNR: PERMISSION_GRANTED");
            }
            return true;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "checkWriteContactsPermissionNR: PERMISSION_DENIED");
        }
        return false;
    }

    /**
     * Check if the user has granted the contacts group permission
     *
     * @param ctx the application context
     * @return true if the permissions have been granted. False if they have been denied or are
     * required to be requested.
     */
    public static boolean checkContactGroupPermissions(@NonNull final Context ctx, Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkContactGroupPermissions");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            switch (hasSelfPermission(ctx, android.Manifest.permission.GET_ACCOUNTS)) {
                case PermissionChecker.PERMISSION_GRANTED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "checkContactGroupPermissions: PERMISSION_GRANTED");
                    }
                    return true;
                case PermissionChecker.PERMISSION_DENIED:
                default:
                    break;
            }
        } else {
            if (checkReadContactPermissionNR(ctx) && checkAccountsPermissionNR(ctx) && checkWriteContactsPermissionNR(ctx)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkContactGroupPermissions: PERMISSION_GRANTED");
                }
                return true;
            }
        }

        if (DEBUG) {
            MyLog.w(CLS_NAME, "checkContactGroupPermissions: PERMISSION_DENIED");
        }

        final Intent intent = new Intent(ctx, ActivityPermissionDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!UtilsBundle.notNaked(bundle)) {
            bundle = new Bundle();
        }
        bundle.putStringArray(REQUESTED_PERMISSION, new String[]{android.Manifest.permission.GET_ACCOUNTS});
        bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_GROUP_CONTACTS);
        intent.putExtras(bundle);
        ctx.startActivity(intent);
        return false;
    }

    /**
     * Check if the user has granted the usage stats permission, which must be manually complete via the
     * settings.
     *
     * @param ctx the application context
     * @return true if the permissions have been granted, false otherwise
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean checkUsageStatsPermission(@NonNull final Context ctx) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkUsageStatsPermission");
        }

        final AppOpsManager appOps = (AppOpsManager) ctx.getSystemService(Context.APP_OPS_SERVICE);

        return appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), ctx.getPackageName()) == AppOpsManager.MODE_ALLOWED;
    }

    public static boolean checkNotificationPolicyPermission(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkNotificationPolicyPermission");
        }
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.N || ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).isNotificationPolicyAccessGranted();
    }

    /**
     * Check if the user has granted bluetooth connect permissions
     *
     * @param ctx the application context
     * @return true if the permissions have been granted. False if they have been denied or are
     * required to be requested.
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    public static boolean checkBluetoothPermissions(@NonNull final Context ctx, @Nullable Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkBluetoothPermissions");
        }

        switch (hasSelfPermission(ctx, android.Manifest.permission.BLUETOOTH_CONNECT)) {
            case PermissionChecker.PERMISSION_GRANTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkBluetoothPermissions: PERMISSION_GRANTED");
                }
                return true;
            case PermissionChecker.PERMISSION_DENIED:
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "checkBluetoothPermissions: PERMISSION_DENIED");
                }

                final Intent intent = new Intent(ctx, ActivityPermissionDialog.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (!UtilsBundle.notNaked(bundle)) {
                    bundle = new Bundle();
                }
                bundle.putStringArray(REQUESTED_PERMISSION, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT});
                bundle.putInt(REQUESTED_PERMISSION_ID, REQUEST_BLUETOOTH_CONNECT);

                intent.putExtras(bundle);

                ctx.startActivity(intent);
                return false;
        }
    }
}
