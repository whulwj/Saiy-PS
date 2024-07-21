package ai.saiy.android.command.alarm;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;

public class CommandAlarm {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandAlarm.class.getSimpleName();

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

    public Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
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
        final AlarmProcess alarmProcess = AlarmHelper.resolve(context, voiceData, supportedLanguage.getLocale());
        outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        if (alarmProcess.isValid()) {
            switch (alarmProcess.getType()) {
                case AlarmProcess.TYPE_HOUR:
                case AlarmProcess.TYPE_MINUTE:
                case AlarmProcess.TYPE_HOUR_MINUTE:
                    if (AlarmHelper.setAlarm(context, alarmProcess.getHourOfDay(), alarmProcess.getMinute(), alarmProcess.getTimeString())) {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(PersonalityResponse.getGenericAcknowledgement(context, supportedLanguage));
                    } else {
                        outcome.setOutcome(Outcome.FAILURE);
                        outcome.setUtterance(context.getString(R.string.error_alarm_schedule));
                    }
                    return returnOutcome(outcome);
            }
        }
        outcome.setOutcome(Outcome.FAILURE);
        if (alarmProcess.outsideTwentyFour()) {
            outcome.setUtterance(context.getString(R.string.error_alarm_duration));
        } else {
            outcome.setUtterance(context.getString(R.string.error_alarm_structure));
        }
        return returnOutcome(outcome);
    }
}
