package ai.saiy.android.command.agenda;

import static ai.saiy.android.utils.UtilsDate.MONTH_OFFSET;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ai.saiy.android.R;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsDate;
import ai.saiy.android.utils.UtilsLocale;
import ai.saiy.android.utils.UtilsString;

public class AgendaProcessHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = AgendaProcessHelper.class.getSimpleName();

    private static AgendaProcess agendaProcess;

    private static void checkYear(@NonNull AgendaProcess agendaProcess) {
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        final int thisMonth = calendar.get(Calendar.MONTH) + MONTH_OFFSET;
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkYear: comparing: " + agendaProcess.getMonth() + " with thisMonth " + thisMonth);
        }
        if (agendaProcess.getMonth() < thisMonth) {
            calendar.add(Calendar.YEAR, 1);
            agendaProcess.setYear(calendar.get(Calendar.YEAR));
            if (DEBUG) {
                MyLog.d(CLS_NAME, "checkYear: cd.year: " + agendaProcess.getYear());
            }
        } else if (agendaProcess.getMonth() > thisMonth) {
            agendaProcess.setYear(calendar.get(Calendar.YEAR));
            if (DEBUG) {
                MyLog.d(CLS_NAME, "checkYear: cd.year: " + agendaProcess.getYear());
            }
        } else {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "checkYear: equal months - check dates");
            }
            final int thisDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            if (agendaProcess.getDayOfMonth() < thisDayOfMonth) {
                calendar.add(Calendar.YEAR, 1);
                agendaProcess.setYear(calendar.get(Calendar.YEAR));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "checkYear: cd.year: " + agendaProcess.getYear());
                }
            } else if (agendaProcess.getDayOfMonth() > thisDayOfMonth) {
                agendaProcess.setYear(calendar.get(Calendar.YEAR));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "checkYear: cd.year: " + agendaProcess.getYear());
                }
            } else {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "checkYear: equal days - return today");
                }
                agendaProcess.setMonth(calendar.get(Calendar.MONTH) + MONTH_OFFSET);
                agendaProcess.setYear(calendar.get(Calendar.YEAR));
            }
        }
        agendaProcess.setHaveYear(true);
    }

    public static AgendaProcess resolve(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull Locale locale) {
        final long then = System.nanoTime();
        agendaProcess = new AgendaProcess();
        boolean isStructured = false;
        final int size = voiceData.size();
        for (int i = 0; i < size; ++i) {
            String vdLower = voiceData.get(i).toLowerCase(locale);
            if (DEBUG) {
                MyLog.d(CLS_NAME, "vdLoop: " + vdLower);
            }
            if (isMonth(context, vdLower)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "isStructured: true");
                }
                isStructured = true;
            }
        }
        if (!isStructured) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "hasStructure: FALSE - Using today");
            }
            final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
            agendaProcess.setOutcome(Outcome.SUCCESS);
            agendaProcess.setWeekday(calendar.get(Calendar.DAY_OF_WEEK));
            agendaProcess.setMonth(calendar.get(Calendar.MONTH) + MONTH_OFFSET);
            agendaProcess.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
            agendaProcess.setYear(calendar.get(Calendar.YEAR));
            agendaProcess.setDate(calendar.getTime());
            agendaProcess.setHaveDate(true);
            agendaProcess.setHaveYear(true);
            agendaProcess.setHaveMonth(true);
            agendaProcess.setIsToday(true);
            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, then);
            }
            return agendaProcess;
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "hasStructure: true");
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "containerDate.haveWeekday: " + agendaProcess.haveWeekday());
            MyLog.d(CLS_NAME, "containerDate.haveDate: " + agendaProcess.haveDate());
            MyLog.d(CLS_NAME, "containerDate.haveMonth: " + agendaProcess.haveMonth());
            MyLog.d(CLS_NAME, "containerDate.haveYear: " + agendaProcess.haveYear());
            MyLog.d(CLS_NAME, "containerDate.monthInt: " + agendaProcess.getMonth());
            MyLog.d(CLS_NAME, "containerDate.dateInt: " + agendaProcess.getDayOfMonth());
            MyLog.d(CLS_NAME, "containerDate.year: " + agendaProcess.getYear());
        }
        if (!UtilsString.notNaked(agendaProcess.getUtterance())) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "SOLVED!");
            }
            agendaProcess.setOutcome(Outcome.SUCCESS);
            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, then);
            }
            return agendaProcess;
        }
        final String result = agendaProcess.getDayOfMonth() + "/" + agendaProcess.getMonth() + "/" + agendaProcess.getYear() + agendaProcess.getUtterance();
        if (DEBUG) {
            MyLog.d(CLS_NAME, "result: " + result);
        }
        agendaProcess.setOutcome(Outcome.FAILURE);
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return agendaProcess;
    }

    private static boolean validateDate(Context context) {
        if (DEBUG) {
            MyLog.d(CLS_NAME, "getWeekday: containerDate.dateInt: " + agendaProcess.getDayOfMonth());
            MyLog.d(CLS_NAME, "getMonth: containerDate.monthInt: " + agendaProcess.getMonth());
            MyLog.d(CLS_NAME, "getMonth: containerDate.year: " + agendaProcess.getYear());
        }
        if (agendaProcess.getMonth() == 0 && agendaProcess.getDayOfMonth() != 0) {
            checkMonth(agendaProcess);
        }
        if (agendaProcess.getYear() == 0 && agendaProcess.getMonth() != 0) {
            checkYear(agendaProcess);
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        if (agendaProcess.getYear() == 0) {
            agendaProcess.setYear(calendar.get(Calendar.YEAR));
        }
        if (agendaProcess.getMonth() == 0) {
            agendaProcess.setMonth(calendar.get(Calendar.MONTH) + MONTH_OFFSET);
        }
        if (agendaProcess.getDayOfMonth() == 0) {
            agendaProcess.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
        }
        String dayOfMonthString;
        String monthString;
        String yearString = String.valueOf(agendaProcess.getYear());
        if (agendaProcess.getMonth() > 0 && agendaProcess.getMonth() < 10) {
            monthString = "0" + agendaProcess.getMonth();
        } else {
            monthString = String.valueOf(agendaProcess.getMonth());
        }
        if (agendaProcess.getDayOfMonth() > 0 && agendaProcess.getDayOfMonth() < 10) {
            dayOfMonthString = "0" + agendaProcess.getDayOfMonth();
        } else {
            dayOfMonthString = String.valueOf(agendaProcess.getDayOfMonth());
        }
        if (DEBUG) {
            MyLog.v(CLS_NAME, "-----------DATE-----------------");
            MyLog.d(CLS_NAME, "validateDate: " + dayOfMonthString + "/" + monthString + "/" + yearString);
            MyLog.v(CLS_NAME, "-----------DATE-----------------");
        }
        final String dateString = dayOfMonthString + monthString + yearString;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddmmyyyy", Locale.US);
            simpleDateFormat.setLenient(false);
            simpleDateFormat.parse(dateString);
            if (agendaProcess.haveWeekday()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "validateDate: containerDate.haveWeekday: true");
                }
                calendar.set(Integer.parseInt(yearString), Integer.parseInt(monthString) - MONTH_OFFSET, Integer.parseInt(dayOfMonthString));
                if (agendaProcess.getWeekday() != calendar.get(Calendar.DAY_OF_WEEK)) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "validateDate: containerDate.dayInt != dotw: " + agendaProcess.getWeekday() + " : " + calendar.get(Calendar.DAY_OF_WEEK));
                    }
                    final int weekday = agendaProcess.getWeekday();
                    switch (weekday) {
                        case 0:
                            return false;
                        case Calendar.SUNDAY:
                        case Calendar.MONTH:
                        case Calendar.TUESDAY:
                        case Calendar.WEDNESDAY:
                        case Calendar.THURSDAY:
                        case Calendar.FRIDAY:
                        case Calendar.SATURDAY:
                            yearString = (agendaProcess.getYear() == Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale()).get(Calendar.YEAR)) ? "" : Constants.SEP_SPACE + agendaProcess.getYear();
                            agendaProcess.setUtterance("The " + UtilsDate.getDayOfMonth(context, agendaProcess.getDayOfMonth()) + " of " + UtilsDate.getMonth(context, agendaProcess.getMonth() - MONTH_OFFSET) + yearString + " is " + context.getString(R.string.not_a) + Constants.SEP_SPACE + UtilsDate.getWeekday(context, weekday));
                            break;
                        default:
                            return false;
                    }
                }
            }

            calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
            calendar.set(agendaProcess.getYear(), agendaProcess.getMonth() - MONTH_OFFSET, agendaProcess.getDayOfMonth(), 0, 0);
            calendar.setTimeZone(TimeZone.getDefault());
            if (DEBUG) {
                MyLog.v(CLS_NAME, "checkDateFormat: True");
                MyLog.v(CLS_NAME, "Cal Ms: " + calendar.getTimeInMillis());
                MyLog.v(CLS_NAME, "Sys Ms:" + System.currentTimeMillis());
                MyLog.v(CLS_NAME, "Cal Start:" + (calendar.getTime().getTime() + 30 * DateUtils.MINUTE_IN_MILLIS));
                MyLog.v(CLS_NAME, "Cal End:" + (calendar.getTime().getTime() + (DateUtils.DAY_IN_MILLIS + 100 * DateUtils.SECOND_IN_MILLIS - 1)));
            }
            agendaProcess.setBeginTime(calendar.getTime().getTime() + 30 * DateUtils.MINUTE_IN_MILLIS);
            agendaProcess.setEndTime(calendar.getTime().getTime() + (DateUtils.DAY_IN_MILLIS + 100 * DateUtils.SECOND_IN_MILLIS - 1));
            agendaProcess.setDate(calendar.getTime());
            return true;
        } catch (ParseException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "checkDateFormat ParseException");
            }
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isMonth(@NonNull Context context, String str) {
        Pair<Boolean, Integer> results = AgendaHelper.isYear(str);
        agendaProcess.setHaveYear(results.first);
        agendaProcess.setYear(results.second);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "containerDate.haveYear: " + agendaProcess.haveYear());
        }
        results = AgendaHelper.isMonth(context, str);
        agendaProcess.setHaveMonth(results.first);
        agendaProcess.setMonth(results.second + MONTH_OFFSET);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "containerDate.haveMonth: " + agendaProcess.haveMonth());
        }
        results = AgendaHelper.isDate(context, str);
        agendaProcess.setHaveDate(results.first);
        agendaProcess.setDayOfMonth(results.second);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "containerDate.haveDate: " + agendaProcess.haveDate());
        }
        results = AgendaHelper.isWeekday(context, str);
        agendaProcess.setHaveWeekday(results.first);
        agendaProcess.setWeekday(results.second);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "containerDate.haveWeekday: " + agendaProcess.haveWeekday());
        }
        if (agendaProcess.haveWeekday()) {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
            switch (agendaProcess.getWeekday()) {
                case AgendaHelper.TODAY:
                    agendaProcess.setWeekday(calendar.get(Calendar.DAY_OF_WEEK));
                    agendaProcess.setMonth(calendar.get(Calendar.MONTH) + MONTH_OFFSET);
                    agendaProcess.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
                    agendaProcess.setYear(calendar.get(Calendar.YEAR));
                    agendaProcess.setDate(calendar.getTime());
                    agendaProcess.setHaveDate(true);
                    agendaProcess.setHaveYear(true);
                    agendaProcess.setHaveMonth(true);
                    agendaProcess.setIsToday(true);
                    return validateDate(context);
                case AgendaHelper.TOMORROW:
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    agendaProcess.setWeekday(calendar.get(Calendar.DAY_OF_WEEK));
                    agendaProcess.setMonth(calendar.get(Calendar.MONTH) + MONTH_OFFSET);
                    agendaProcess.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
                    agendaProcess.setYear(calendar.get(Calendar.YEAR));
                    agendaProcess.setHaveDate(true);
                    agendaProcess.setHaveYear(true);
                    agendaProcess.setHaveMonth(true);
                    agendaProcess.setIsTomorrow(true);
                    return validateDate(context);
                case AgendaHelper.DAY_AFTER_TOMORROW:
                    calendar.add(Calendar.DAY_OF_YEAR, 2);
                    agendaProcess.setWeekday(calendar.get(Calendar.DAY_OF_WEEK));
                    agendaProcess.setMonth(calendar.get(Calendar.MONTH) + MONTH_OFFSET);
                    agendaProcess.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
                    agendaProcess.setYear(calendar.get(Calendar.YEAR));
                    agendaProcess.setHaveDate(true);
                    agendaProcess.setHaveYear(true);
                    agendaProcess.setHaveMonth(true);
                    return validateDate(context);
                default:
                    if (!agendaProcess.haveDate()) {
                        AgendaHelper.updateWeekday(calendar, agendaProcess.getWeekday());
                        agendaProcess.setMonth(calendar.get(Calendar.MONTH) + MONTH_OFFSET);
                        agendaProcess.setDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
                        agendaProcess.setYear(calendar.get(Calendar.YEAR));
                        agendaProcess.setHaveDate(true);
                        agendaProcess.setHaveYear(true);
                        agendaProcess.setHaveMonth(true);
                    }
                    break;
            }
        }

        if (agendaProcess.haveMonth() && agendaProcess.haveDate() && agendaProcess.haveWeekday()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "(A) Needed parameters");
            }
            if (!agendaProcess.haveYear()) {
                checkYear(agendaProcess);
            }
            return validateDate(context);
        }
        if (!agendaProcess.haveMonth() && !agendaProcess.haveDate() && !agendaProcess.haveWeekday()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "(10)isMonth: isDate: isWeekday: FALSE");
            }
            return false;
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "(1)isMonth: or isDate: or isWeekday: true");
        }
        if (!agendaProcess.haveWeekday()) {
            if (agendaProcess.haveDate() && agendaProcess.haveMonth() && agendaProcess.haveYear()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "(7a) - Have date, month, year - All good");
                }
                return validateDate(context);
            }
            if (agendaProcess.haveDate() && !agendaProcess.haveMonth()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "(7)isMonth: or isDate: or isWeekday: TRUE : isDate: TRUE: isMonth: FALSE");
                    MyLog.i(CLS_NAME, "(7) - Have date, but no month or weekday");
                }
                checkMonth(agendaProcess);
                return validateDate(context);
            }
            if (agendaProcess.haveMonth() && !agendaProcess.haveDate()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "(8)isMonth: or isDate: or isWeekday: TRUE : isMonth: TRUE: isDate: FALSE");
                    MyLog.i(CLS_NAME, "(8) - A month only - can't schedule for that");
                }
                return false;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "(9)isMonth: or isDate: or isWeekday: TRUE : isDate: TRUE: isMonth: TRUE");
                MyLog.i(CLS_NAME, "(9) - Have month and date, but no year - Resolve Year");
            }
            checkYear(agendaProcess);
            return validateDate(context);
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "(2)isMonth: or isDate: or isWeekday: TRUE : isWeekday: TRUE");
        }
        if (!agendaProcess.haveMonth() && !agendaProcess.haveDate()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "(3)isMonth: or isDate: or isWeekday: TRUE : isWeekday: TRUE: isMonth: FALSE: isDate: FALSE");
                MyLog.i(CLS_NAME, "(3) - Next weekday");
            }
            return validateDate(context);
        }
        if (!agendaProcess.haveMonth() && agendaProcess.haveDate()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "(4)isMonth: or isDate: or isWeekday: TRUE : isWeekday: TRUE: isMonth: FALSE: isDate: TRUE");
                MyLog.i(CLS_NAME, "(4) - Have day and date - Resolve month");
            }
            checkMonth(agendaProcess);
            return validateDate(context);
        }
        if (agendaProcess.haveMonth() && agendaProcess.haveDate()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "(5)isMonth: or isDate: or isWeekday: TRUE : isWeekday: TRUE: isMonth: TRUE: isDate: TRUE");
                MyLog.i(CLS_NAME, "(5) - Have weekday, month and date - Resolving year.");
            }
            checkYear(agendaProcess);
            return validateDate(context);
        }
        if (!agendaProcess.haveMonth() || agendaProcess.haveDate()) {
            return false;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "(6)isMonth: or isDate: or isWeekday: TRUE : isWeekday: TRUE: isMonth: TRUE: isDate: FALSE - Nonsensical");
        }
        return false;
    }

    private static void checkMonth(@NonNull AgendaProcess agendaProcess) {
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), UtilsLocale.getDefaultLocale());
        final int thisDate = calendar.get(Calendar.DAY_OF_MONTH);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "checkMonth: comparing: " + agendaProcess.getDayOfMonth() + " with thisDate " + thisDate);
        }
        if (agendaProcess.getDayOfMonth() < thisDate) {
            calendar.add(Calendar.MONTH, 1);
            agendaProcess.setMonth(calendar.get(Calendar.MONTH) + MONTH_OFFSET);
            agendaProcess.setYear(calendar.get(Calendar.YEAR));
            if (DEBUG) {
                MyLog.d(CLS_NAME, "checkMonth: cd.monthInt+1: " + agendaProcess.getMonth());
                MyLog.d(CLS_NAME, "checkMonth: cd.year: " + agendaProcess.getYear());
            }
        } else if (agendaProcess.getDayOfMonth() > thisDate) {
            agendaProcess.setMonth(calendar.get(Calendar.MONTH) + MONTH_OFFSET);
            agendaProcess.setYear(calendar.get(Calendar.YEAR));
            if (DEBUG) {
                MyLog.d(CLS_NAME, "checkMonth: cd.monthInt: " + agendaProcess.getMonth());
                MyLog.d(CLS_NAME, "checkMonth: cd.year: " + agendaProcess.getYear());
            }
        } else {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "checkMonth: equal days - return today");
            }
            agendaProcess.setYear(calendar.get(Calendar.YEAR));
            agendaProcess.setMonth(calendar.get(Calendar.MONTH) + MONTH_OFFSET);
        }
        agendaProcess.setHaveYear(true);
        agendaProcess.setHaveMonth(true);
    }
}
