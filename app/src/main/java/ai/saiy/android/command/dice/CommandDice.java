package ai.saiy.android.command.dice;

import android.content.Context;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;

public class CommandDice {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandDice.class.getSimpleName();

    private static final byte MAX_DICE_OPTIONS = 6;
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
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "isResolved: false");
        }
        final long result = new Dice(supportedLanguage).sortDice(context, voiceData);
        if (result < 0 || result >= 99999) {
            outcome.setUtterance("[dice]" + String.format(ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.dice_too_many), new BigDecimal(Double.toString(result * 3.5d)).setScale(0, RoundingMode.HALF_UP).toString()));
            outcome.setOutcome(Outcome.SUCCESS);
            outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
            return returnOutcome(outcome);
        }
        int dice;
        final Random random = new Random();
        if (result > 1) {
            dice = 0;
            for (int i = 0; i < result; ++i) {
                dice += (random.nextInt(MAX_DICE_OPTIONS) + 1);
            }
        } else {
            dice = (random.nextInt(MAX_DICE_OPTIONS) + 1);
        }
        outcome.setUtterance("[dice]" + dice);
        outcome.setOutcome(Outcome.SUCCESS);
        outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        return returnOutcome(outcome);
    }
}
