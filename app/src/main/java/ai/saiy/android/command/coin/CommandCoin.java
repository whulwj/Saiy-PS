package ai.saiy.android.command.coin;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Random;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;

public final class CommandCoin {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandCoin.class.getSimpleName();

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
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "isResolved: false");
        }
        final String[] coins = ai.saiy.android.localisation.SaiyResourcesHelper.getArrayResource(context, supportedLanguage, R.array.array_heads_tails);
        outcome.setUtterance("[coin]" + coins[new Random().nextInt(coins.length)]);
        outcome.setOutcome(Outcome.SUCCESS);
        outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        return returnOutcome(outcome);
    }
}
