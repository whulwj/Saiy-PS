package ai.saiy.android.command.hardware;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.NonNull;

import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsReflection;

public class WifiManagerHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = WifiManagerHelper.class.getSimpleName();

    private void switchState(@NonNull Context context, @NonNull WifiManager wifiManager, boolean enabled) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "switchState");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (enabled) {
                UtilsReflection.invokeMethod(connectivityManager, ConnectivityManager.class, "startTethering", new Class[]{Integer.TYPE, Boolean.TYPE, StartTetheringCallbackWrapper.sStartTetheringCallbackClass}, 0/*ConnectivityManager.TETHERING_WIFI*/, false, new StartTetheringCallbackWrapper() {
                    @Override
                    public void onTetheringFailed() {
                        super.onTetheringFailed();
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "onTetheringFailed");
                        }
                    }
                }.getProxyInstance());
            } else {
                UtilsReflection.invokeMethod(connectivityManager, ConnectivityManager.class, "stopTethering", new Class[]{Integer.TYPE}, 0/*ConnectivityManager.TETHERING_WIFI*/);
            }
        } else {
            UtilsReflection.invokeMethod(wifiManager, WifiManager.class, "setWifiApEnabled", new Class[]{Boolean.TYPE}, enabled);
        }
    }

    public boolean isEnabled(@NonNull WifiManager wifiManager) {
        Object object = UtilsReflection.invokeMethod(wifiManager, WifiManager.class, "isWifiApEnabled");
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        object = UtilsReflection.invokeMethod(wifiManager, WifiManager.class, "getWifiApState");
        if (object instanceof Integer) {
            return (Integer) object != 11; /*WifiManager#WIFI_AP_STATE_DISABLED*/
        }
        return false;
    }

    public void disable(@NonNull Context context, @NonNull WifiManager wifiManager) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "disable");
        }
        switchState(context, wifiManager, false);
    }

    public void enable(@NonNull Context context, @NonNull WifiManager wifiManager) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "enable");
        }
        switchState(context, wifiManager, true);
    }
}
