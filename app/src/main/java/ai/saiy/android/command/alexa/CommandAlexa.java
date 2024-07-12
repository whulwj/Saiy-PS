package ai.saiy.android.command.alexa;

import android.content.Context;

import java.util.ArrayList;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class CommandAlexa {
    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandAlexa.class.getSimpleName();

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

    public Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        String query = null;
        if (!cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            final ArrayList<String> queries = new Alexa(supportedLanguage).sort(context, voiceData);
            if (!queries.isEmpty()) {
                query = queries.get(0);
            } else if (DEBUG) {
                MyLog.v(CLS_NAME, "Alexa data empty");
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "isResolved: true");
        }
        if (UtilsString.notNaked(query)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "query: " + query);
            }
            outcome.setUtterance(query);
            outcome.setOutcome(Outcome.SUCCESS);
        } else {
            outcome.setOutcome(Outcome.FAILURE);
        }
        return returnOutcome(outcome);
    }
}
