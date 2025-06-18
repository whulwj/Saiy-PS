package ai.saiy.android.command.notification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.command.settings.SettingsIntent;
import ai.saiy.android.localisation.SaiyResourcesHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;

public final class CommandNotifications {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandNotifications.class.getSimpleName();

    private long then;

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(@NonNull Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }

    public @NonNull Outcome getResponse(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage, @NonNull ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        outcome.setOutcome(Outcome.SUCCESS);
        final boolean announceNotifications = ai.saiy.android.utils.SPH.getAnnounceNotifications(context);
        boolean isAnnounceNotificationRunning = false;
        for (String packageName : NotificationManagerCompat.getEnabledListenerPackages(context)) {
            if (packageName.equals(context.getPackageName())) {
                isAnnounceNotificationRunning = true;
                break;
            }
        }
        if (isAnnounceNotificationRunning) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "notification listener service running");
            }
            if (announceNotifications) {
                outcome.setUtterance(String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.notification_response), SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.no_longer)));
            } else {
                outcome.setUtterance(String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.notification_response), ""));
            }
        } else if (announceNotifications) {
            outcome.setUtterance(String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.notification_response), SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.no_longer)));
        } else if (SettingsIntent.settingsIntent(context, SettingsIntent.Type.NOTIFICATION_ACCESS)) {
            outcome.setUtterance(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.notifications_enable));
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "notification listener: settings location unknown");
            }
            UtilsApplication.openApplicationSpecificSettings(context, context.getPackageName());
            outcome.setUtterance(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.settings_missing));
        }
        ai.saiy.android.utils.SPH.setAnnounceNotifications(context, !announceNotifications);
        return returnOutcome(outcome);
    }
}
