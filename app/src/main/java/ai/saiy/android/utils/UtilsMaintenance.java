package ai.saiy.android.utils;

import android.content.Context;
import android.os.Debug;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.schedulers.Schedulers;

public class UtilsMaintenance {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UtilsMaintenance.class.getSimpleName();

    public static void restart(final Context context) {
        debugInfo();
        Schedulers.computation().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "restart run");
                }
                SPH.setSelfAwareEnabled(context, true);
                ai.saiy.android.service.helper.SelfAwareHelper.restartService(context);
            }
        }, 3500L, TimeUnit.MILLISECONDS);
    }

    public static void shutdown(final Context context) {
        debugInfo();
        Schedulers.computation().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "shutdown run");
                }
                SPH.setSelfAwareEnabled(context, false);
                ai.saiy.android.service.helper.SelfAwareHelper.stopService(context);
                System.exit(0);
            }
        }, 3500L, TimeUnit.MILLISECONDS);
    }

    private static void debugInfo() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "VM Heap Size: " + Runtime.getRuntime().totalMemory());
            MyLog.i(CLS_NAME, "Allocated VM Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            MyLog.i(CLS_NAME, "Remaining VM Heap Size: " + Runtime.getRuntime().maxMemory());
            MyLog.i(CLS_NAME, "Native Allocated Memory: " + Debug.getNativeHeapAllocatedSize());
        }
    }
}
