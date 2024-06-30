package ai.saiy.android.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ai.saiy.android.ui.notification.NotificationHelper;
import ai.saiy.android.utils.MyLog;

public class BRBirthday extends BroadcastReceiver {
    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = BRBirthday.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onReceive");
        }
        NotificationHelper.createBirthdayNotification(context);
    }
}
