package ai.saiy.android.command.settings.system;

import android.content.Context;

import java.util.ArrayList;

import ai.saiy.android.command.settings.SettingsIntent;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;

public class CommandSettings {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandSettings.class.getSimpleName();

    public Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        final long then = System.nanoTime();
        final Outcome outcome = new Outcome();
        if (!cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            final SettingsIntent settingsIntent = new Settings(supportedLanguage).detectSettingsIntent(context, voiceData);
            if (settingsIntent.getType() == SettingsIntent.Type.UNKNOWN) {
                outcome.setOutcome(Outcome.FAILURE);
                outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getUnknownSettingsError(context, supportedLanguage));
            } else if (SettingsIntent.settingsIntent(context, settingsIntent.getType())) {
                outcome.setOutcome(Outcome.SUCCESS);
                outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getDisplayedSettings(context, supportedLanguage, settingsIntent.getCommand()));
            } else {
                outcome.setOutcome(Outcome.FAILURE);
                outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getDisplaySettingsError(context, supportedLanguage, settingsIntent.getCommand()));
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "isResolved: true");
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }
}
