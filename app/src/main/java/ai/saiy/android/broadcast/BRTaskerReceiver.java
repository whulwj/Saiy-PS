package ai.saiy.android.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.cognitive.identity.provider.microsoft.Speaker;
import ai.saiy.android.custom.TaskerVariable;
import ai.saiy.android.custom.TaskerVariableHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.service.helper.SelfAwareHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsBundle;
import ai.saiy.android.utils.UtilsString;
import ai.saiy.android.utils.UtilsToast;

public class BRTaskerReceiver extends BroadcastReceiver {
    public static final String EXTRA_ACTION = "ai.saiy.android.extra.ACTION";
    public static final String EXTRA_VARIABLE_COUNT = "ai.saiy.android.extra.VARIABLE_COUNT";
    public static final String EXTRA_VAR_ONLY = "ai.saiy.android.extra.VAR_ONLY";
    public static final String EXTRA_VAR_NAME_ = "ai.saiy.android.extra.VAR_NAME_";
    public static final String EXTRA_VAR_VALUE_ = "ai.saiy.android.extra.VAR_VALUE_";
    public static final String EXTRA_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE";
    public static final String EXTRA_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB";

    public static final int ACTION_SPEAK = 1;
    public static final int ACTION_NOTIFY = 2;
    public static final int ACTION_ENABLE_HOTWORD = 3;
    public static final int ACTION_DISABLE_HOTWORD = 4;
    public static final int ACTION_ENABLE_DRIVING_PROFILE = 5;
    public static final int ACTION_DISABLE_DRIVING_PROFILE = 6;
    public static final int ACTION_ENABLE_NOTIFICATIONS = 7;
    public static final int ACTION_DISABLE_NOTIFICATIONS = 8;
    public static final int ACTION_ACTIVATE = 9;
    public static final int ACTION_SHUTDOWN = 10;
    @IntDef({ACTION_SPEAK, ACTION_NOTIFY, ACTION_ENABLE_HOTWORD, ACTION_DISABLE_HOTWORD, ACTION_ENABLE_DRIVING_PROFILE,
            ACTION_DISABLE_DRIVING_PROFILE, ACTION_ENABLE_NOTIFICATIONS, ACTION_DISABLE_NOTIFICATIONS, ACTION_ACTIVATE, ACTION_SHUTDOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Action {}

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = BRTaskerReceiver.class.getSimpleName();

    private void showVariableError(final Context context) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    UtilsToast.showToast(context, R.string.tasker_error_send_values, Toast.LENGTH_SHORT);
                }
            });
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    final Toast makeText = Toast.makeText(context, R.string.tasker_error_send_values, Toast.LENGTH_SHORT);
                    final View view = makeText.getView();
                    if (view != null) {
                        final TextView textView = view.findViewById(android.R.id.message);
                        textView.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_launcher, 0, 0, 0);
                        textView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
                        textView.setCompoundDrawablePadding(12);
                    } else {
                        makeText.setText(context.getString(R.string.app_name) + ": " + context.getString(R.string.tasker_error_send_values));
                    }
                    makeText.show();
                }
            });
        }
    }

    private void examineBundle(Bundle bundle) {
        MyLog.i(CLS_NAME, "examineBundle");
        if (bundle == null) {
            MyLog.w(CLS_NAME, "examineBundle: bundle null");
            return;
        }
        for (String str : bundle.keySet()) {
            MyLog.v(CLS_NAME, "examineBundle: " + str + " ~ " + bundle.get(str));
        }
    }

    private boolean isVariableName(String str) {
        return str.startsWith("%");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onReceive");
        }
        if (!"com.twofortyfouram.locale.intent.action.FIRE_SETTING".equals(intent.getAction())) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "error: " + String.format("Received unexpected Intent action %s", intent.getAction()));
            }
            return;
        }
        if (UtilsBundle.isSuspicious(intent)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "intent isSuspicious");
            }
            return;
        }
        final Bundle forwardedBundle = intent.getBundleExtra(BRTaskerReceiver.EXTRA_BUNDLE);
        if (UtilsBundle.isSuspicious(forwardedBundle)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "bundle isSuspicious");
            }
            return;
        }
        if (DEBUG) {
            examineBundle(forwardedBundle);
        }
        final String input = forwardedBundle.getString(Speaker.EXTRA_VALUE, null);
        if (DEBUG) {
            MyLog.d(CLS_NAME, "input: " + input);
        }
        if (!UtilsString.notNaked(input)) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "input is null");
            }
            return;
        }

        if (forwardedBundle.getBoolean(EXTRA_VAR_ONLY, false)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "BUNDLE_EXTRA_VAR_ONLY");
            }
            final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(SPH.getVRLocale(context));
            final int requestedVariableCount = forwardedBundle.getInt(EXTRA_VARIABLE_COUNT, -1);
            if (DEBUG) {
                MyLog.d(CLS_NAME, "requestedVariableCount: " + requestedVariableCount);
            }
            if (requestedVariableCount < 1) {
                showVariableError(context);
                return;
            }
            final ArrayList<TaskerVariable> taskerVariableArray = new ArrayList<>(requestedVariableCount);
            final String empty_tasker_value = ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, sl, R.string.empty_tasker_value);
            boolean haveVariableError = false;
            for (int i = 0; i < requestedVariableCount; i++) {
                String name = BRTaskerReceiver.EXTRA_VAR_NAME_ + i;
                String value = BRTaskerReceiver.EXTRA_VAR_VALUE_ + i;
                if (forwardedBundle.containsKey(value) && forwardedBundle.containsKey(name)) {
                    String variableName = forwardedBundle.getString(name, null);
                    String variableValue = forwardedBundle.getString(value, null);
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "bundlePair: " + variableName + " ~ " + variableValue);
                    }
                    if (UtilsString.notNaked(variableName)) {
                        if (!UtilsString.notNaked(variableValue)) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "bundleVariableValue naked: replacing");
                            }
                            haveVariableError = true;
                            variableValue = empty_tasker_value;
                        } else if (isVariableName(variableValue)) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "bundleVariableValue unpopulated var: replacing");
                            }
                            haveVariableError = true;
                            variableValue = empty_tasker_value;
                        }
                        taskerVariableArray.add(new TaskerVariable(variableName, variableValue));
                    } else {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "bundleVariableName: skipping");
                        }
                        haveVariableError = true;
                    }
                } else {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "missing keys: " + name + " ~ " + value);
                    }
                    haveVariableError = true;
                }
            }
            if (haveVariableError) {
                showVariableError(context);
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "taskerVariableArraySize: " + taskerVariableArray.size());
            }
            for (TaskerVariable taskerVariable : taskerVariableArray) {
                TaskerVariableHelper.setTaskerVariable(context, taskerVariable, sl, -1L);
            }
            return;
        }

        final boolean startRecognition = forwardedBundle.getBoolean(Speaker.EXTRA_START_VR, false);
        final Locale locale = ai.saiy.android.utils.UtilsLocale.stringToLocale(forwardedBundle.getString(Speaker.EXTRA_LOCALE));
        if (DEBUG) {
            MyLog.d(CLS_NAME, "startRecognition: " + startRecognition);
            MyLog.d(CLS_NAME, "locale: " + locale);
        }
        LocalRequest localRequest;
        switch (forwardedBundle.getInt(BRTaskerReceiver.EXTRA_ACTION, ACTION_SPEAK)) {
            case ACTION_SPEAK:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "ACTION_SPEAK");
                }
                final SupportedLanguage sl = SupportedLanguage.getSupportedLanguage(locale);
                localRequest = new LocalRequest(context);
                localRequest.prepareDefault(startRecognition ? LocalRequest.ACTION_SPEAK_LISTEN : LocalRequest.ACTION_SPEAK_ONLY, sl, locale, locale, input);
                localRequest.execute();
                break;
            case ACTION_NOTIFY:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "ACTION_NOTIFY");
                }
                ai.saiy.android.ui.notification.NotificationHelper.createTaskerNotification(context, input, locale, startRecognition);
                break;
            case ACTION_ENABLE_HOTWORD:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "ACTION_ENABLE_HOTWORD");
                }
                localRequest = new LocalRequest(context);
                localRequest.prepareDefault(LocalRequest.ACTION_START_HOTWORD, null);
                localRequest.execute();
                break;
            case ACTION_DISABLE_HOTWORD:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "ACTION_DISABLE_HOTWORD");
                }
                localRequest = new LocalRequest(context);
                localRequest.prepareDefault(LocalRequest.ACTION_STOP_HOTWORD, null);
                localRequest.execute();
                break;
            case ACTION_ENABLE_DRIVING_PROFILE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "ACTION_ENABLE_DRIVING_PROFILE");
                }
                localRequest = new LocalRequest(context);
                localRequest.prepareDefault(LocalRequest.ACTION_TOGGLE_DRIVING_PROFILE, null);
                localRequest.execute();
                break;
            case ACTION_DISABLE_DRIVING_PROFILE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "ACTION_DISABLE_DRIVING_PROFILE");
                }
                localRequest = new LocalRequest(context);
                localRequest.prepareDefault(LocalRequest.ACTION_TOGGLE_DRIVING_PROFILE, null);
                localRequest.execute();
                break;
            case ACTION_ENABLE_NOTIFICATIONS:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "ACTION_ENABLE_NOTIFICATIONS");
                }
                SPH.setAnnounceNotifications(context, true);
                break;
            case ACTION_DISABLE_NOTIFICATIONS:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "ACTION_DISABLE_NOTIFICATIONS");
                }
                SPH.setAnnounceNotifications(context, false);
                break;
            case ACTION_ACTIVATE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "ACTION_ACTIVATE");
                }
                SelfAwareHelper.startSelfAwareIfRequired(context);
                break;
            case ACTION_SHUTDOWN:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "ACTION_SHUTDOWN");
                }
                SelfAwareHelper.stopService(context);
                break;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "ACTION_DEFAULT");
                }
                break;
        }
    }
}
