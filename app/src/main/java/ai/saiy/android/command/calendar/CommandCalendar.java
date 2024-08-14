package ai.saiy.android.command.calendar;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.command.alarm.AlarmHelper;
import ai.saiy.android.command.alarm.AlarmProcess;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;

public class CommandCalendar {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandCalendar.class.getSimpleName();

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
        final CalendarProcess calendarProcess = CalendarProcessHelper.resolve(context, voiceData, supportedLanguage);
        if (!calendarProcess.haveHourOrMinute) {
            outcome.setOutcome(calendarProcess.outcome);
            outcome.setUtterance(calendarProcess.utterance);
            return returnOutcome(outcome);
        }
        final AlarmProcess alarmProcess = AlarmHelper.resolve(context, voiceData, supportedLanguage.getLocale());
        if (alarmProcess.isValid()) {
            switch (alarmProcess.getType()) {
                case AlarmProcess.TYPE_HOUR:
                case AlarmProcess.TYPE_MINUTE:
                case AlarmProcess.TYPE_HOUR_MINUTE:
                    if (AlarmHelper.setAlarm(context, alarmProcess.getHourOfDay(), alarmProcess.getMinute(), alarmProcess.getTimeString())) {
                        outcome.setOutcome(Outcome.SUCCESS);
                        outcome.setUtterance(context.getString(R.string.calendar_to_alarm));
                        return returnOutcome(outcome);
                    }
                default:
                    outcome.setOutcome(Outcome.FAILURE);
                    outcome.setUtterance(context.getString(R.string.calendar_error_structure));
                    return returnOutcome(outcome);
            }
        }
        outcome.setOutcome(Outcome.FAILURE);
        outcome.setUtterance(context.getString(R.string.calendar_error_structure));
        return returnOutcome(outcome);
    }
}
