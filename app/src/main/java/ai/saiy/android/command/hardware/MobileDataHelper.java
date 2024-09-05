package ai.saiy.android.command.hardware;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;

import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsReflection;

public class MobileDataHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = MobileDataHelper.class.getSimpleName();

    private static final String SET_MOBILE_DATA_ENABLED = "setMobileDataEnabled";

    private void switchState(@NonNull Context context, boolean enabled) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "switchState");
        }
        try {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final Method method = UtilsReflection.getMethod(ConnectivityManager.class, SET_MOBILE_DATA_ENABLED, Boolean.TYPE);
            if (method != null) {
                try {
                    method.invoke(connectivityManager, enabled);
                } catch (Throwable t) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, SET_MOBILE_DATA_ENABLED + " " + t.getClass().getSimpleName() + ", " + t.getMessage());
                    }
                }
            } else {
                final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                UtilsReflection.invokeMethod(telephonyManager, TelephonyManager.class, "setDataEnabled", new Class[]{Boolean.TYPE}, enabled);
            }
            for (int i = 0; i < 10; i++) {
                Thread.sleep(100L);
                if (isEnabled(connectivityManager) == enabled) {
                    return;
                }
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "Attempt: " + i);
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "switchState: Exception " + e.getMessage());
            }
        }
    }

    private boolean isEnabled(@NonNull ConnectivityManager connectivityManager) {
        try {
            final Object object = UtilsReflection.invokeMethod(connectivityManager, ConnectivityManager.class, "getMobileDataEnabled");
            if (object instanceof Boolean) {
                return (boolean) object;
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "isEnabled: Exception " + e.getMessage());
            }
        }
        return false;
    }

    public boolean isEnabled(@NonNull Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return isEnabled(connectivityManager);
    }

    public void disable(@NonNull Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "disable");
        }
        switchState(context, false);
    }

    public void enable(@NonNull Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "enable");
        }
        switchState(context, true);
    }

    public boolean hasTelephonyFeature(@NonNull Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }
}
