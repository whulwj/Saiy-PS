package ai.saiy.android.command.remember;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;

public final class CommandRemember {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandRemember.class.getSimpleName();

    private final Outcome outcome = new Outcome();

    public @NonNull Outcome getResponse(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage, @NonNull ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        long then = System.nanoTime();
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
            final CommandRememberValues commandRememberValues = (CommandRememberValues) cr.getVariableData();
            final ai.saiy.android.processing.Qubit qubit = new ai.saiy.android.processing.Qubit();
            qubit.setClipboardContent(commandRememberValues.getClipboardContent());
            outcome.setQubit(qubit);
            outcome.setOutcome(Outcome.SUCCESS);
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getClipboardRemember(context, supportedLanguage));
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            final ArrayList<String> things = new Remember(supportedLanguage).fetchTarget(context, voiceData);
            if (things.isEmpty()) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "remember data empty");
                }
                outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getRememberError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
            } else {
                ai.saiy.android.processing.Qubit qubit = new ai.saiy.android.processing.Qubit();
                qubit.setClipboardContent(things.get(0));
                outcome.setQubit(qubit);
                outcome.setOutcome(Outcome.SUCCESS);
                outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getClipboardRemember(context, supportedLanguage));
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }
}
