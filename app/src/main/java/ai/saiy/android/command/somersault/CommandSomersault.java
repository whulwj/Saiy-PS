package ai.saiy.android.command.somersault;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.command.orientation.OrientationHelper;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;

public class CommandSomersault {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandSomersault.class.getSimpleName();

    public @NonNull Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        final long then = System.nanoTime();
        final Outcome outcome = new Outcome();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
            if (ai.saiy.android.intent.ExecuteIntent.settingsIntent(context, IntentConstants.MANAGE_WRITE_SETTINGS)) {
                outcome.setUtterance(context.getString(R.string.permission_write_settings));
            } else {
                UtilsApplication.openApplicationSpecificSettings(context, context.getPackageName());
                outcome.setUtterance(context.getString(R.string.settings_missing, context.getString(R.string.content_modify_system_settings)));
            }
            outcome.setOutcome(Outcome.SUCCESS);
        } else if (!cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            if (new OrientationHelper().somersault(context, new Somersault(supportedLanguage).detectSomersault(context, voiceData))) {
                outcome.setOutcome(Outcome.SUCCESS);
                outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getSomersault(context, supportedLanguage));
            } else {
                outcome.setOutcome(Outcome.FAILURE);
                outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getSomersaultError(context, supportedLanguage));
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
