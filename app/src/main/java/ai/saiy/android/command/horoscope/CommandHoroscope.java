package ai.saiy.android.command.horoscope;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.ui.activity.ActivityShowDialog;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

public class CommandHoroscope {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandHoroscope.class.getSimpleName();

    private long then;

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(@NonNull Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, this.then);
        }
        return outcome;
    }

    public Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        CommandHoroscopeValues commandHoroscopeValues;
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
            commandHoroscopeValues = (CommandHoroscopeValues) cr.getVariableData();
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            commandHoroscopeValues = new Horoscope(supportedLanguage).fetch(context, voiceData);
        }
        if (commandHoroscopeValues == null) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getHoroscopeError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        CommandHoroscopeValues.Sign sign = commandHoroscopeValues.getSign();
        if (sign == CommandHoroscopeValues.Sign.UNKNOWN && (sign = SPH.getStarSign(context)) == CommandHoroscopeValues.Sign.UNKNOWN) {
            ai.saiy.android.intent.ExecuteIntent.saiyActivity(context, ActivityShowDialog.class, null, true);
            outcome.setUtterance(context.getString(R.string.horoscope_dob_request));
            outcome.setOutcome(Outcome.SUCCESS);
            return returnOutcome(outcome);
        }
        final String description = new HoroscopeHelper().execute(sign);
        if (!UtilsString.notNaked(description)) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getHoroscopeError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        final long successCount = SPH.getHoroscopeIncrement(context);
        final String utterance = (successCount <= 0 || successCount % 5 != 0) ? sign.getName() + ". " + description : ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.horoscope_provider_fyf) + ". " + sign.getName() + ". " + description;
        SPH.horoscopeAutoIncrease(context);
        outcome.setUtterance(utterance);
        outcome.setOutcome(Outcome.SUCCESS);
        return returnOutcome(outcome);
    }
}
