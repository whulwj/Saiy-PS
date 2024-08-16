package ai.saiy.android.command.application.foreground;

import android.content.Context;
import android.os.Build;
import android.util.Pair;

import androidx.annotation.NonNull;

import ai.saiy.android.R;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.command.settings.SettingsIntent;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class CommandForeground {
    private static final boolean DEBUG = MyLog.DEBUG;

    private long then;

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(@NonNull Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CommandForeground.class.getSimpleName(), then);
        }
        return outcome;
    }

    public @NonNull Outcome getResponse(Context context, SupportedLanguage supportedLanguage) {
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !ai.saiy.android.permissions.PermissionHelper.checkUsageStatsPermission(context)) {
            if (ai.saiy.android.intent.ExecuteIntent.settingsIntent(context, IntentConstants.SETTINGS_USAGE_STATS)) {
                outcome.setUtterance(context.getString(R.string.app_speech_usage_stats));
            } else {
                SettingsIntent.settingsIntent(context, SettingsIntent.Type.DEVICE);
                outcome.setUtterance(context.getString(R.string.issue_usage_stats_bug));
            }
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        final String foregroundPackage = UtilsApplication.getForegroundPackage(context, 180000L);
        if (!UtilsString.notNaked(foregroundPackage)) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getForegroundUnknownError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        final Pair<Boolean, String> appNamePair = UtilsApplication.getAppNameFromPackage(context, foregroundPackage);
        if (!appNamePair.first) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getForegroundUnknownError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        outcome.setOutcome(Outcome.SUCCESS);
        if (appNamePair.second.matches(context.getString(R.string.app_name))) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getForegroundSaiy(context, supportedLanguage));
        } else {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getForeground(context, supportedLanguage, appNamePair.second));
        }
        return returnOutcome(outcome);
    }
}
