package ai.saiy.android.command.timer;

import android.content.Context;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.processing.Position;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public final class CommandTimer {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandTimer.class.getSimpleName();

    private final ai.saiy.android.processing.EntangledPair entangledPair = new ai.saiy.android.processing.EntangledPair(Position.TOAST_LONG, CC.COMMAND_TIMER);
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
        outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "isResolved: false");
        }
        final ArrayList<String> timers = new Timer(supportedLanguage).sortTimer(context, voiceData);
        if (!UtilsList.notNaked(timers)) {
            outcome.setUtterance(context.getString(R.string.error_timer_establish));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        final CommandTimerValue commandTimerValue = TimerHelper.resolveAlarm(context, timers, supportedLanguage.getLocale());
        if (!commandTimerValue.isValid()) {
            outcome.setUtterance(context.getString(R.string.error_timer_structure));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        if (commandTimerValue.getRunningTotal() >= (DateUtils.DAY_IN_MILLIS / DateUtils.SECOND_IN_MILLIS)) {
            outcome.setUtterance(context.getString(R.string.error_timer_duration));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        if (!TimerHelper.setTimer(context, commandTimerValue.getRunningTotal(), commandTimerValue.getCallee())) {
            outcome.setOutcome(Outcome.FAILURE);
            outcome.setUtterance(context.getString(R.string.error_timer_schedule));
            return returnOutcome(outcome);
        }

        final StringBuilder sb = new StringBuilder();
        sb.append(context.getString(R.string.timer_set_for));
        sb.append(Constants.SEP_SPACE);
        if (commandTimerValue.getHour() != 0) {
            sb.append(commandTimerValue.getHour());
            sb.append(Constants.SEP_SPACE);
            if (commandTimerValue.getHour() == 1) {
                sb.append(context.getString(R.string.hour));
            } else {
                sb.append(context.getString(R.string.hours));
            }
            sb.append(Constants.SEP_SPACE);
        }
        if (commandTimerValue.getMinute() != 0) {
            sb.append(commandTimerValue.getMinute());
            sb.append(Constants.SEP_SPACE);
            if (commandTimerValue.getMinute() == 1) {
                sb.append(context.getString(R.string.minute));
            } else {
                sb.append(context.getString(R.string.minutes));
            }
            sb.append(Constants.SEP_SPACE);
        }
        if (commandTimerValue.getSecond() != 0) {
            sb.append(commandTimerValue.getSecond());
            sb.append(Constants.SEP_SPACE);
            if (commandTimerValue.getSecond() == 1) {
                sb.append(context.getString(R.string.second));
            } else {
                sb.append(context.getString(R.string.seconds));
            }
            sb.append(".");
        }
        entangledPair.setToastContent(sb.toString());
        outcome.setEntangledPair(entangledPair);
        outcome.setOutcome(Outcome.SUCCESS);
        outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getGenericAcknowledgement(context, supportedLanguage));
        return returnOutcome(outcome);
    }
}
