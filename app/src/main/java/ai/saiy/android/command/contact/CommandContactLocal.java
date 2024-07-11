package ai.saiy.android.command.contact;

import android.content.Context;

import java.util.ArrayList;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class CommandContactLocal {
    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandContactLocal.class.getSimpleName();

    public ai.saiy.android.processing.Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        final long then = System.nanoTime();
        ai.saiy.android.processing.Outcome outcome = new ai.saiy.android.processing.Outcome();
        if (!cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            outcome = new CommandContact().getResponse(context, voiceData, supportedLanguage, cr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "isResolved: true");
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }
}
