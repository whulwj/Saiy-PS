package ai.saiy.android.command.calendar;

import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.UtilsDate;

public class CalendarProcess {
    boolean haveHourOrMinute;
    boolean allDay;
    boolean haveYear;
    boolean haveMonth;
    boolean haveWeekday;
    boolean haveDate;
    boolean haveHour;
    boolean haveMinute;
    boolean isAM;
    boolean isPM;
    int hourOfDay = 0;
    int minute = 0;
    /**
     * The valid month starts from {@link UtilsDate#MONTH_OFFSET}
     */
    int month = 0;
    int weekday = 0;
    int dayOfMonth = 0;
    int year = 0;
    @Outcome.Result int outcome = Outcome.SUCCESS;
    String extraWeekdayDescription = "";
    String utterance = "";
}
