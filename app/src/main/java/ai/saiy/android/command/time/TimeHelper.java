package ai.saiy.android.command.time;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Pair;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsDate;

public class TimeHelper {
    private static int minuteInt = 0;
    private static int hourInt = 0;
    private static String hour = "null";
    private static String minute = "null";
    private static String format = "null";

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = TimeHelper.class.getSimpleName();

    private int getNextHour() {
        int nextHour = hourInt + 1;
        if (nextHour == 13) {
            nextHour = 1;
        }
        return nextHour;
    }

    private String formatTime(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "formatTime");
        }
        if (minuteInt == 1) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 1");
            }
            minute += XMLResultsHandler.SEP_SPACE + context.getString(R.string.MINUTE_PAST);
            format = "mh";
        }
        if (minuteInt == 5 || minuteInt == 10 || minuteInt == 20 || minuteInt == 25) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 5/10/15/20");
            }
            minute += XMLResultsHandler.SEP_SPACE + context.getString(R.string.PAST);
            format = "mh";
        }
        if (minuteInt == 15) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 15");
            }
            minute = context.getString(R.string.QUARTER_PAST);
            format = "mh";
        }
        if (minuteInt == 30) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 30");
            }
            minute = context.getString(R.string.HALF_PAST);
            format = "mh";
        }
        if (minuteInt > 1 && minuteInt < 30 && minuteInt != 5 && minuteInt != 10 && minuteInt != 15 && minuteInt != 20 && minuteInt != 25) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 2-29");
            }
            minute += XMLResultsHandler.SEP_SPACE + context.getString(R.string.MINUTES_PAST);
            format = "mh";
        }
        if (minuteInt > 30 && minuteInt < 50 && minuteInt != 35 && minuteInt != 40 && minuteInt != 45) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 31-49");
            }
            format = "hm";
        }
        if (minuteInt == 35) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 35");
            }
            minute = context.getString(R.string.TWENTY_FIVE_TO);
            hour = String.valueOf(getNextHour());
            format = "mh";
        }
        if (minuteInt == 40) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 40");
            }
            minute = context.getString(R.string.TWENTY_TO);
            hour = String.valueOf(getNextHour());
            format = "mh";
        }
        if (minuteInt == 45) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 45");
            }
            minute = context.getString(R.string.QUARTER_TO);
            hour = String.valueOf(getNextHour());
            format = "mh";
        }
        if (minuteInt == 50) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 50");
            }
            minute = context.getString(R.string.TEN_TO);
            hour = String.valueOf(getNextHour());
            format = "mh";
        }
        if (minuteInt == 51) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 51");
            }
            minute = "9 " + context.getString(R.string.MINUTES_TO);
            hour = String.valueOf(getNextHour());
            format = "mh";
        }
        if (minuteInt == 52) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 52");
            }
            minute = "8 " + context.getString(R.string.MINUTES_TO);
            hour = String.valueOf(getNextHour());
            format = "mh";
        }
        if (minuteInt == 53) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 53");
            }
            minute = "7 " + context.getString(R.string.MINUTES_TO);
            hour = String.valueOf(getNextHour());
            format = "mh";
        }
        if (minuteInt == 54) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 54");
            }
            minute = "6 " + context.getString(R.string.MINUTES_TO);
            hour = String.valueOf(getNextHour());
            format = "mh";
        }
        if (minuteInt == 55) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 55");
            }
            minute = context.getString(R.string.FIVE_TO);
            hour = String.valueOf(getNextHour());
            format = "mh";
        }
        if (minuteInt == 56) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 56");
            }
            minute = "4 " + context.getString(R.string.MINUTES_TO);
            hour = String.valueOf(getNextHour());
            format = "mh";
        }
        if (minuteInt == 57) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 53");
            }
            minute = "3 " + context.getString(R.string.MINUTES_TO);
            hour = String.valueOf(getNextHour());
            format = "mh";
        }
        if (minuteInt == 58) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 53");
            }
            minute = "2 " + context.getString(R.string.MINUTES_TO);
            hour = String.valueOf(getNextHour());
            format = "mh";
        }
        if (minuteInt == 59) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 53");
            }
            minute = "1 " + context.getString(R.string.MINUTE_TO);
            hour = String.valueOf(getNextHour());
            format = "mh";
        }
        if (minuteInt == 0) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "MIN 00");
            }
            minute = XMLResultsHandler.SEP_SPACE + context.getString(R.string.O__CLOCK);
            hour = context.getString(R.string.EXACTLY) + XMLResultsHandler.SEP_SPACE + hour;
            format = "hm";
        }
        if (format.matches("mh")) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "mh: " + minute + XMLResultsHandler.SEP_SPACE + hour);
            }
            return context.getString(R.string.it_s) + XMLResultsHandler.SEP_SPACE + minute + XMLResultsHandler.SEP_SPACE + hour;
        } else if (format.matches("hm")) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hm: " + hour + XMLResultsHandler.SEP_SPACE + minute);
            }
            return context.getString(R.string.it_s) + XMLResultsHandler.SEP_SPACE + hour + XMLResultsHandler.SEP_SPACE + minute;
        }
        return "";
    }

    public Pair<String, String> formatSpokenTimeIn(Context context, String dateTime) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(dateTime));
        } catch (ParseException e) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "formatSpokenTimeIn: ParseException");
                e.printStackTrace();
            }
            calendar.setTimeInMillis(System.currentTimeMillis());
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "formatSpokenTimeIn: " + calendar.get(Calendar.DAY_OF_WEEK));
            MyLog.i(CLS_NAME, "formatSpokenTimeIn: " + calendar.get(Calendar.HOUR));
            MyLog.i(CLS_NAME, "formatSpokenTimeIn: " + calendar.get(Calendar.MINUTE));
        }
        int hour = calendar.get(Calendar.HOUR);
        if (hour == 0) {
            hour = 12;
        }
        final int minute = calendar.get(Calendar.MINUTE);
        final String minuteString = (minute == 0) ? "" : minute < 10 ? "O " + minute : String.valueOf(minute);
        final String string = (calendar.get(Calendar.AM_PM) == Calendar.AM)? context.getString(R.string.time_am) :  context.getString(R.string.time_pm);
        return new Pair<>(UtilsDate.getWeekday(context, calendar.get(Calendar.DAY_OF_WEEK)), hour + XMLResultsHandler.SEP_SPACE + minuteString + XMLResultsHandler.SEP_SPACE + string);
    }

    public String getTime(Context context) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getTime");
        }
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        final SimpleDateFormat hourOfDayFormat = new SimpleDateFormat("h", Locale.getDefault());
        final SimpleDateFormat minuteFormat = new SimpleDateFormat("m", Locale.getDefault());
        hour = hourOfDayFormat.format(calendar.getTime());
        minute = minuteFormat.format(calendar.getTime());
        minuteInt = Integer.parseInt(minute);
        hourInt = Integer.parseInt(hour);
        return formatTime(context);
    }

    public String getTime(Context context, long millis) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getTime");
        }
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        calendar.setTimeInMillis(millis);
        final SimpleDateFormat hourOfDayFormat = new SimpleDateFormat("h", Locale.getDefault());
        final SimpleDateFormat minuteFormat = new SimpleDateFormat("m", Locale.getDefault());
        hour = hourOfDayFormat.format(calendar.getTime());
        minute = minuteFormat.format(calendar.getTime());
        minuteInt = Integer.parseInt(minute);
        hourInt = Integer.parseInt(hour);
        return formatTime(context).replaceFirst(context.getString(R.string.it_s) + XMLResultsHandler.SEP_SPACE, "");
    }

    public String getDate(Context context, SupportedLanguage supportedLanguage, long millis) {
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        calendar.setTimeInMillis(millis);
        final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        final String today = sr.getString(R.string.today);
        if (DateUtils.isToday(millis)) {
            sr.reset();
            return today;
        }
        final String yesterday = sr.getString(R.string.yesterday);
        final String of = sr.getString(R.string.of);
        final String the = sr.getString(R.string.the);
        final String on = sr.getString(R.string.on);
        sr.reset();
        final Calendar lastMonthCalendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        lastMonthCalendar.setTimeInMillis(millis);
        lastMonthCalendar.add(Calendar.DAY_OF_MONTH, -1);
        return (lastMonthCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && lastMonthCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) && lastMonthCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
                ? yesterday : on + XMLResultsHandler.SEP_SPACE + UtilsDate.getWeekday(context, calendar.get(Calendar.DAY_OF_WEEK)) + XMLResultsHandler.SEP_SPACE + the + XMLResultsHandler.SEP_SPACE + UtilsDate.getDayOfMonth(context, calendar.get(Calendar.DAY_OF_MONTH)) + XMLResultsHandler.SEP_SPACE + of + XMLResultsHandler.SEP_SPACE + UtilsDate.getMonth(context, calendar.get(Calendar.MONTH));
    }
}
