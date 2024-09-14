package ai.saiy.android.utils;

import android.content.Context;

import java.util.Calendar;

import ai.saiy.android.R;

public class UtilsDate {
    public static final int MONTH_OFFSET = 1;

    public static String getWeekday(Context context, int weekday) {
        switch (weekday) {
            case Calendar.SUNDAY:
                return context.getString(R.string.SUNDAY);
            case Calendar.MONDAY:
                return context.getString(R.string.MONDAY);
            case Calendar.TUESDAY:
                return context.getString(R.string.TUESDAY);
            case Calendar.WEDNESDAY:
                return context.getString(R.string.WEDNESDAY);
            case Calendar.THURSDAY:
                return context.getString(R.string.THURSDAY);
            case Calendar.FRIDAY:
                return context.getString(R.string.FRIDAY);
            case Calendar.SATURDAY:
                return context.getString(R.string.SATURDAY);
            default:
                return "";
        }
    }

    public static String getDayOfMonth(Context context, int dayOfMonth) {
        switch (dayOfMonth) {
            case 1:
                return context.getString(R.string.FIRST);
            case 2:
                return context.getString(R.string.SECOND);
            case 3:
                return context.getString(R.string.THIRD);
            case 4:
                return context.getString(R.string.FOURTH);
            case 5:
                return context.getString(R.string.FIFTH);
            case 6:
                return context.getString(R.string.SIXTH);
            case 7:
                return context.getString(R.string.SEVENTH);
            case 8:
                return context.getString(R.string.EIGHTH);
            case 9:
                return context.getString(R.string.NINTH);
            case 10:
                return context.getString(R.string.TENTH);
            case 11:
                return context.getString(R.string.ELEVENTH);
            case 12:
                return context.getString(R.string.TWELFTH);
            case 13:
                return context.getString(R.string.THIRTEENTH);
            case 14:
                return context.getString(R.string.FOURTEENTH);
            case 15:
                return context.getString(R.string.FIFTEENTH);
            case 16:
                return context.getString(R.string.SIXTEENTH);
            case 17:
                return context.getString(R.string.SEVENTEENTH);
            case 18:
                return context.getString(R.string.EIGHTEENTH);
            case 19:
                return context.getString(R.string.NINETEENTH);
            case 20:
                return context.getString(R.string.TWENTIETH);
            case 21:
                return context.getString(R.string.TWENTY_FIRST);
            case 22:
                return context.getString(R.string.TWENTY_SECOND);
            case 23:
                return context.getString(R.string.TWENTY_THIRD);
            case 24:
                return context.getString(R.string.TWENTY_FOURTH);
            case 25:
                return context.getString(R.string.TWENTY_FIFTH);
            case 26:
                return context.getString(R.string.TWENTY_SIXTH);
            case 27:
                return context.getString(R.string.TWENTY_SEVENTH);
            case 28:
                return context.getString(R.string.TWENTY_EIGHTH);
            case 29:
                return context.getString(R.string.TWENTY_NINTH);
            case 30:
                return context.getString(R.string.THIRTIETH);
            case 31:
                return context.getString(R.string.THIRTY_FIRST);
            default:
                return "";
        }
    }

    public static String getMonth(Context context, int month) {
        switch (month) {
            case Calendar.JANUARY:
                return context.getString(R.string.JANUARY);
            case Calendar.FEBRUARY:
                return context.getString(R.string.FEBRUARY);
            case Calendar.MARCH:
                return context.getString(R.string.MARCH);
            case Calendar.APRIL:
                return context.getString(R.string.APRIL);
            case Calendar.MAY:
                return context.getString(R.string.MAY);
            case Calendar.JUNE:
                return context.getString(R.string.JUNE);
            case Calendar.JULY:
                return context.getString(R.string.JULY);
            case Calendar.AUGUST:
                return context.getString(R.string.AUGUST);
            case Calendar.SEPTEMBER:
                return context.getString(R.string.SEPTEMBER);
            case Calendar.OCTOBER:
                return context.getString(R.string.OCTOBER);
            case Calendar.NOVEMBER:
                return context.getString(R.string.NOVEMBER);
            case Calendar.DECEMBER:
                return context.getString(R.string.DECEMBER);
            default:
                return "";
        }
    }
}
