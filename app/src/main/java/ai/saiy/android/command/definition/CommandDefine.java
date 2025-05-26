package ai.saiy.android.command.definition;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ai.saiy.android.command.definition.online.DefinitionHelper;
import ai.saiy.android.command.definition.online.DefinitionResponse;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class CommandDefine {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandDefine.class.getSimpleName();

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

    public @NonNull Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        String word;
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
            word = ((CommandDefineValues) cr.getVariableData()).getWord();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            final List<String> words = new Define(supportedLanguage).sortWord(context, voiceData);
            if (!words.isEmpty()) {
                word = words.get(0);
            } else {
                word = null;
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "definition data empty");
                }
            }
        }
        if (!UtilsString.notNaked(word)) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getDefineError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        final Pair<Boolean, DefinitionResponse> responsePair = new DefinitionHelper().execute(context, word);
        if (responsePair.first) {
            outcome.setUtterance(responsePair.second.getWord() + ". " + responsePair.second.getText());
            outcome.setOutcome(Outcome.SUCCESS);
            return returnOutcome(outcome);
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "responsePair: false");
        }
        outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getDefineUnknownError(context, supportedLanguage));
        outcome.setOutcome(Outcome.FAILURE);
        return returnOutcome(outcome);
    }
}
