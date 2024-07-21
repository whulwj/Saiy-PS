package ai.saiy.android.ui.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import ai.saiy.android.R;
import ai.saiy.android.service.helper.AssistantIntentService;
import ai.saiy.android.ui.activity.ActivityLauncherShortcut;
import ai.saiy.android.utils.MyLog;

public class WidgetProvider extends AppWidgetProvider {
    public static final int REQUEST_CODE = 32;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = WidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onUpdate");
        }
        for (int widgetId : appWidgetIds) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onUpdate: appWidgetId: " + widgetId);
            }
            Intent intent = new Intent(context, ActivityLauncherShortcut.class);
            intent.setAction(AssistantIntentService.ACTION_WIDGET_ASSIST);
            PendingIntent activity = PendingIntent.getActivity(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_assist);
            remoteViews.setOnClickPendingIntent(R.id.widgetAssistIcon, activity);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
