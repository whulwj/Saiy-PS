package ai.saiy.android.command.superuser;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;

public class CommandSuperuser {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandSuperuser.class.getSimpleName();

    private long then;

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }

    public @NonNull Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        CommandSuperuserValues commandSuperuserValues;
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
            commandSuperuserValues = (CommandSuperuserValues) cr.getVariableData();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            commandSuperuserValues = new Superuser(supportedLanguage).sort(context, voiceData);
        }
        if (commandSuperuserValues != null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "root value: " + commandSuperuserValues.getRoot().name());
            }
            //TODO
        }
        outcome.setOutcome(Outcome.SUCCESS);
        outcome.setUtterance(context.getString(R.string.superuser_disabled));
        return returnOutcome(outcome);
    }
}
