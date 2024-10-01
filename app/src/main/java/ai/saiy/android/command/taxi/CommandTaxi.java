package ai.saiy.android.command.taxi;

import android.content.Context;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import ai.saiy.android.R;
import ai.saiy.android.applications.Installed;
import ai.saiy.android.localisation.SaiyResourcesHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;

public final class CommandTaxi {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandTaxi.class.getSimpleName();

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

    public @NonNull Outcome getResponse(Context context, SupportedLanguage supportedLanguage) {
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        outcome.setOutcome(Outcome.SUCCESS);
        if (ai.saiy.android.intent.ExecuteIntent.orderTaxi(context)) {
            outcome.setUtterance(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.okay));
        } else {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getNoAppError(context, supportedLanguage) + XMLResultsHandler.SEP_SPACE + SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.suggest_install_uber));
            ai.saiy.android.intent.ExecuteIntent.playStoreSearch(context, Installed.PACKAGE_UBER);
        }
        return returnOutcome(outcome);
    }
}
