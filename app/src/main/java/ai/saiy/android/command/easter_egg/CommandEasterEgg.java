package ai.saiy.android.command.easter_egg;

import android.content.Context;

import androidx.annotation.NonNull;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;

public final class CommandEasterEgg {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandEasterEgg.class.getSimpleName();

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

    public @NonNull Outcome getResponse(@NonNull Context context, @NonNull SupportedLanguage supportedLanguage) {
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        outcome.setUtterance("[wand] " + EasterEggLocal.getEasterEggStage(context, supportedLanguage, ai.saiy.android.utils.SPH.getEasterEggState(context)));
        if (ai.saiy.android.utils.SPH.getEasterEggState(context) >= EasterEggHunter.STAGE_7) {
            outcome.setCondition(Condition.CONDITION_NONE);
            outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        } else {
            outcome.setCondition(Condition.CONDITION_EASTER_EGG);
            outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
        }
        outcome.setOutcome(Outcome.SUCCESS);
        return returnOutcome(outcome);
    }
}
