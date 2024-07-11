package ai.saiy.android.command.alarm;

import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.utils.MyLog;

public class AlarmHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = AlarmHelper.class.getSimpleName();

    private static AlarmProcess alarmProcess;
    private static boolean hasHourOrMinute = false;
    private static boolean hasTime = false;

    private static Pattern pCalled;
    private static Pattern pNamed;
    private static Pattern pNames;
    private static Pattern pName;
    private static Pattern pCalls;
    private static Pattern pCall;
    private static Pattern pCold;
    private static String called;
    private static String named;
    private static String names;
    private static String name;
    private static String calls;
    private static String call;
    private static String cold;

    private static String IN_THE_EVENING;
    private static String FIVE_TO;
    private static String TEN_TO;
    private static String NUMERIC_TEN_TO;
    private static String QUARTER_TO;
    private static String TWENTY_TO;
    private static String NUMERIC_TWENTY_FIVE_TO;
    private static String TWENTY_FIVE_TO;
    private static String HALF_PAST;
    private static String TWENTY_FIVE_PAST;
    private static String TWENTY_NUMERIC_FIVE_PAST;
    private static String NUMERIC_TWENTY_FIVE_PAST;
    private static String TWENTY_PAST;
    private static String NUMERIC_TWENTY_PAST;
    private static String QUARTER_PAST;
    private static String TEN_PAST;
    private static String NUMERIC_TEN_PAST;
    private static String FIVE_PAST;
    private static String NUMERIC_FIVE_PAST;
    private static String TO_MINUTES;
    private static String FOR_MINUTES;

    private static Pattern pMONDAY;
    private static Pattern pTUESDAY;
    private static Pattern pWEDNESDAY;
    private static Pattern pTHURSDAY;
    private static Pattern pFRIDAY;
    private static Pattern pSATURDAY;
    private static Pattern pSUNDAY;
    private static Pattern pTOMORROW;
    private static Pattern pTODAY;
    private static String minutes;
    private static String minute;
    private static String hour;
    private static String _MORNING;
    private static String _EVENING;
    private static String _NIGHT;
    private static String _AFTERNOON;
    private static String O__CLOCK;
    private static String O_CLOCK;
    private static String IN_THE_MORNING;
    private static String IN_THE_AFTERNOON;

    private static final Pattern p3Digits = Pattern.compile(".*\\b(\\d{3})\\b.*");
    private static final Pattern p4Digits = Pattern.compile(".*\\b(\\d{4})\\b.*");
    private static final Pattern p3DigitsGap = Pattern.compile(".*\\b(\\d\\s\\d{2})\\b.*");
    private static final Pattern p4DigitsGap = Pattern.compile(".*\\b(\\d{2}\\s\\d{2})\\b.*");
    private static final Pattern p1DigitAMNS = Pattern.compile(".*\\b\\dam\\b.*");
    private static final Pattern p2DigitAMNS = Pattern.compile(".*\\b\\d\\dam\\b.*");
    private static final Pattern p1DigitPMNS = Pattern.compile(".*\\b\\dpm\\b.*");
    private static final Pattern p2DigitPMNS = Pattern.compile(".*\\b\\d\\dpm\\b.*");
    private static final Pattern p1DigitAMDOT = Pattern.compile(".*\\b\\d[a.m.]\\b.*");
    private static final Pattern p2DigitAMDOT = Pattern.compile(".*\\b\\d\\d[a.m.]\\b.*");
    private static final Pattern p1DigitPMDOT = Pattern.compile(".*\\b\\d[p.m.]\\b.*");
    private static final Pattern p2DigitPMDOT = Pattern.compile(".*\\b\\d\\d[p.m.]\\b.*");
    private static final Pattern p1DigitSAMDOT = Pattern.compile(".*\\b\\d\\s[a.m.]\\b.*");
    private static final Pattern p2DigitSAMDOT = Pattern.compile(".*\\b\\d\\d\\s[a.m.]\\b.*");
    private static final Pattern p1DigitSPMDOT = Pattern.compile(".*\\b\\d\\s[p.m.]\\b.*");
    private static final Pattern p2DigitSPMDOT = Pattern.compile(".*\\b\\d\\d\\s[p.m.]\\b.*");
    private static final Pattern p1DigitAM = Pattern.compile(".*\\b\\d\\sam\\b.*");
    private static final Pattern p1DigitASM = Pattern.compile(".*\\b\\d\\sa\\sm\\b.*");
    private static final Pattern p2DigitAM = Pattern.compile(".*\\b\\d\\d\\sam\\b.*");
    private static final Pattern p2DigitASM = Pattern.compile(".*\\b\\d\\d\\sa\\sm\\b.*");
    private static final Pattern p1DigitPM = Pattern.compile(".*\\b\\d\\spm\\b.*");
    private static final Pattern p1DigitPSM = Pattern.compile(".*\\b\\d\\sp\\sm\\b.*");
    private static final Pattern p2DigitPM = Pattern.compile(".*\\b\\d\\d\\spm\\b.*");
    private static final Pattern p2DigitPSM = Pattern.compile(".*\\b\\d\\d\\sp\\sm\\b.*");
    private static final Pattern pAM = Pattern.compile(".*\\bam\\b.*");
    private static final Pattern pPM = Pattern.compile(".*\\bpm\\b.*");
    private static final Pattern pAMDOT = Pattern.compile(".*\\ba[.m.]\\b.*");
    private static final Pattern pPMDOT = Pattern.compile(".*\\bp[.m.]\\b.*");
    private static final Pattern pAM1DDOT = Pattern.compile(".*\\b\\da[.m.]\\b.*");
    private static final Pattern pPM1DDOT = Pattern.compile(".*\\b\\dp[.m.]\\b.*");
    private static final Pattern pAM2DDOT = Pattern.compile(".*\\b\\d\\da[.m.]\\b.*");
    private static final Pattern pPM2DDOT = Pattern.compile(".*\\b\\d\\dp[.m.]\\b.*");
    private static final Pattern pAMS = Pattern.compile(".*\\ba\\sm\\b.*");
    private static final Pattern pPMS = Pattern.compile(".*\\bp\\sm\\b.*");
    private static final Pattern pAM1D = Pattern.compile(".*\\b\\dam\\b.*");
    private static final Pattern pPM1D = Pattern.compile(".*\\b\\dpm\\b.*");
    private static final Pattern pPM2D = Pattern.compile(".*\\b\\d\\dpm\\b.*");
    private static final Pattern pAM2D = Pattern.compile(".*\\b\\d\\dam\\b.*");

    private static int getType(String str) {
        if (str.contains(hour) && str.contains(minute)) {
            return AlarmProcess.TYPE_HOUR_MINUTE;
        }
        return str.contains(hour) ? AlarmProcess.TYPE_HOUR : AlarmProcess.TYPE_MINUTE;
    }

    public static AlarmProcess resolve(Context context, ArrayList<String> voiceData, Locale locale) {
        final long then = System.nanoTime();
        alarmProcess = new AlarmProcess();
        if (called == null || pCalled == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "resolveAlarm: initialising strings");
            }
            initStrings(context);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "resolveAlarm: strings initialised");
        }
        String vdLower;
        String str = "";
        for (int i = 0, size = voiceData.size(); i < size; ++i) {
            vdLower = voiceData.get(i).toLowerCase(locale);
            if (DEBUG) {
                MyLog.d(CLS_NAME, "alarmTime: " + vdLower);
            }
            str = vdLower.replaceAll(":", "").replace(TO_MINUTES, "2 " + minutes).replace(FOR_MINUTES, "4 " + minutes);
            hasTime = false;
            hasHourOrMinute = false;
            if (checkTime(str)) {
                break;
            }
        }
        if (hasHourOrMinute) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "structureHM");
            }
            alarmProcess.setValidness(true);
            alarmProcess.setType(getType(str));
            Pair<Integer, Integer> timeFromNow;
            switch (alarmProcess.getType()) {
                case AlarmProcess.TYPE_HOUR:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "TYPE_HOUR");
                    }
                    timeFromNow = getTime(AlarmProcess.TYPE_HOUR, dissectHour(str), 0);
                    alarmProcess.setHourOfDay(timeFromNow.first);
                    alarmProcess.setMinute(timeFromNow.second);
                    break;
                case AlarmProcess.TYPE_MINUTE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "TYPE_MINUTE");
                    }
                    timeFromNow = getTime(AlarmProcess.TYPE_MINUTE, 0, dissectMinute(str));
                    alarmProcess.setHourOfDay(timeFromNow.first);
                    alarmProcess.setMinute(timeFromNow.second);
                    break;
                case AlarmProcess.TYPE_HOUR_MINUTE:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "TYPE_HOUR_MINUTE");
                    }
                    final Pair<Integer, Integer> hourAndMinute = dissectHourAndMinute(str);
                    timeFromNow = getTime(AlarmProcess.TYPE_HOUR_MINUTE, hourAndMinute.first, hourAndMinute.second);
                    alarmProcess.setHourOfDay(timeFromNow.first);
                    alarmProcess.setMinute(timeFromNow.second);
                    break;
            }
            alarmProcess.setTimeString(getTimes(str));
            if (DEBUG) {
                MyLog.i(CLS_NAME, "alarmProcess.hourInt: " + alarmProcess.getHourOfDay());
                MyLog.i(CLS_NAME, "alarmProcess.minuteInt: " + alarmProcess.getMinute());
                MyLog.i(CLS_NAME, "alarmProcess.called: " + alarmProcess.getTimeString());
                MyLog.i(CLS_NAME, "alarmProcess.result: " + alarmProcess.isValid());
                MyLog.getElapsed(CLS_NAME, then);
            }
            return alarmProcess;
        }
        if (!hasTime) {
            alarmProcess.setValidness(false);
            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, then);
            }
            return alarmProcess;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "structure");
        }
        if (alarmProcess.isPM() && alarmProcess.getHourOfDay() < 13) {
            increaseTwelveHours(alarmProcess.getHourOfDay());
        } else if (alarmProcess.isAM() && alarmProcess.getHourOfDay() == 12) {
            alarmProcess.setHourOfDay(0);
        } else if (!alarmProcess.isPM() && !alarmProcess.isAM() && alarmProcess.getHourOfDay() < 13 && alarmProcess.getHourOfDay() > 0) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "have no am or pm: checking: " + alarmProcess.getHourOfDay());
            }
            final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "currentHour: " + currentHour);
            }
            final int hour24 = alarmProcess.getHourOfDay() + 12;
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hour24: " + hour24);
            }
            if (currentHour > alarmProcess.getHourOfDay()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "current hour greater than requested hour");
                }
                if (currentHour > hour24) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "current hour greater than hour24: leaving");
                    }
                } else if (currentHour != hour24) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "current hour less than hour24: setting hour24");
                    }
                    alarmProcess.setHourOfDay(hour24);
                } else if (calendar.get(Calendar.MINUTE) < alarmProcess.getMinute()) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "current minute less than requested minute: setting hour24");
                    }
                    alarmProcess.setHourOfDay(hour24);
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "current minute greater than requested minute: leaving");
                }
            } else if (DEBUG) {
                MyLog.i(CLS_NAME, "current hour less than requested hour: leaving");
            }
        }
        final Pair<Boolean, Integer> weekdayPair = isWeekday(str);
        if (weekdayPair.first && !withinTwentyFour(weekdayPair.second)) {
            alarmProcess.setValidness(false);
            alarmProcess.setOutsideTwentyFour(true);
            if (DEBUG) {
                MyLog.getElapsed(CLS_NAME, then);
            }
            return alarmProcess;
        }
        alarmProcess.setValidness(true);
        alarmProcess.setType(AlarmProcess.TYPE_HOUR_MINUTE);
        alarmProcess.setTimeString(getTimes(str));
        if (DEBUG) {
            MyLog.i(CLS_NAME, "alarmProcess.hourInt: " + alarmProcess.getHourOfDay());
            MyLog.i(CLS_NAME, "alarmProcess.minuteInt: " + alarmProcess.getMinute());
            MyLog.i(CLS_NAME, "alarmProcess.called: " + alarmProcess.getTimeString());
            MyLog.i(CLS_NAME, "alarmProcess.result: " + alarmProcess.isValid());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return alarmProcess;
    }

    private static @NonNull Pair<Integer, Integer> getTime(int type, int hourOfDay, int minute) {
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        if (DEBUG) {
            MyLog.i(CLS_NAME, "Original = " + calendar.getTime());
        }
        switch (type) {
            case AlarmProcess.TYPE_HOUR:
                try {
                    calendar.add(Calendar.HOUR, hourOfDay);
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getTime: NFE: TYPE_HOUR");
                    }
                }
                break;
            case AlarmProcess.TYPE_MINUTE:
                try {
                    calendar.add(Calendar.MINUTE, minute + 1);
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getTime: NFE: TYPE_MINUTE");
                    }
                }
                break;
            case AlarmProcess.TYPE_HOUR_MINUTE:
                try {
                    calendar.add(Calendar.MINUTE, (hourOfDay * 60) + minute + 1);
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "getTime: NFE: TYPE_HOUR_MINUTE");
                    }
                }
                break;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "Final = " + calendar.getTime());
            MyLog.i(CLS_NAME, "Calendar.HOUR_OF_DAY " + calendar.get(Calendar.HOUR_OF_DAY));
            MyLog.i(CLS_NAME, "Calendar.MINUTE " + calendar.get(Calendar.MINUTE));
        }
        return new Pair<>(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    private static void initStrings(Context context) {
        called = context.getString(R.string.called);
        named = context.getString(R.string.named);
        names = context.getString(R.string.names);
        name = context.getString(R.string.name);
        calls = context.getString(R.string.calls);
        call = context.getString(R.string.call);
        cold = context.getString(R.string.cold);
        pCalled = Pattern.compile(".*\\b" + called + "\\b.*");
        pNamed = Pattern.compile(".*\\b" + named + "\\b.*");
        pNames = Pattern.compile(".*\\b" + names + "\\b.*");
        pName = Pattern.compile(".*\\b" + name + "\\b.*");
        pCalls = Pattern.compile(".*\\b" + calls + "\\b.*");
        pCall = Pattern.compile(".*\\b" + call + "\\b.*");
        pCold = Pattern.compile(".*\\b" + cold + "\\b.*");
        pMONDAY = Pattern.compile(".*\\b" + context.getString(R.string.MONDAY) + "\\b.*");
        pTUESDAY = Pattern.compile(".*\\b" + context.getString(R.string.TUESDAY) + "\\b.*");
        pWEDNESDAY = Pattern.compile(".*\\b" + context.getString(R.string.WEDNESDAY) + "\\b.*");
        pTHURSDAY = Pattern.compile(".*\\b" + context.getString(R.string.THURSDAY) + "\\b.*");
        pFRIDAY = Pattern.compile(".*\\b" + context.getString(R.string.FRIDAY) + "\\b.*");
        pSATURDAY = Pattern.compile(".*\\b" + context.getString(R.string.SATURDAY) + "\\b.*");
        pSUNDAY = Pattern.compile(".*\\b" + context.getString(R.string.SUNDAY) + "\\b.*");
        pTODAY = Pattern.compile(".*\\b" + context.getString(R.string.TODAY) + "\\b.*");
        pTOMORROW = Pattern.compile(".*\\b" + context.getString(R.string.TOMORROW) + "\\b.*");
        minutes = context.getString(R.string.minutes);
        minute = context.getString(R.string.minute);
        hour = context.getString(R.string.hour);
        _MORNING = context.getString(R.string._MORNING);
        _EVENING = context.getString(R.string._EVENING);
        _NIGHT = context.getString(R.string._NIGHT);
        _AFTERNOON = context.getString(R.string._AFTERNOON);
        TO_MINUTES = context.getString(R.string.TO_MINUTES);
        FOR_MINUTES = context.getString(R.string.FOR_MINUTES);
        O__CLOCK = context.getString(R.string.O__CLOCK);
        O_CLOCK = context.getString(R.string.O_CLOCK);
        IN_THE_MORNING = context.getString(R.string.IN_THE_MORNING);
        IN_THE_AFTERNOON = context.getString(R.string.IN_THE_AFTERNOON);
        IN_THE_EVENING = context.getString(R.string.IN_THE_EVENING);
        FIVE_TO = context.getString(R.string.FIVE_TO);
        TEN_TO = context.getString(R.string.TEN_TO);
        NUMERIC_TEN_TO = context.getString(R.string.NUMERIC_TEN_TO);
        QUARTER_TO = context.getString(R.string.QUARTER_TO);
        TWENTY_TO = context.getString(R.string.TWENTY_TO);
        NUMERIC_TWENTY_FIVE_TO = context.getString(R.string.NUMERIC_TWENTY_FIVE_TO);
        TWENTY_FIVE_TO = context.getString(R.string.TWENTY_FIVE_TO);
        HALF_PAST = context.getString(R.string.HALF_PAST);
        TWENTY_FIVE_PAST = context.getString(R.string.TWENTY_FIVE_PAST);
        TWENTY_NUMERIC_FIVE_PAST = context.getString(R.string.TWENTY_NUMERIC_FIVE_PAST);
        NUMERIC_TWENTY_FIVE_PAST = context.getString(R.string.NUMERIC_TWENTY_FIVE_PAST);
        TWENTY_PAST = context.getString(R.string.TWENTY_PAST);
        NUMERIC_TWENTY_PAST = context.getString(R.string.NUMERIC_TWENTY_PAST);
        QUARTER_PAST = context.getString(R.string.QUARTER_PAST);
        TEN_PAST = context.getString(R.string.TEN_PAST);
        NUMERIC_TEN_PAST = context.getString(R.string.NUMERIC_TEN_PAST);
        FIVE_PAST = context.getString(R.string.FIVE_PAST);
        NUMERIC_FIVE_PAST = context.getString(R.string.NUMERIC_FIVE_PAST);
    }

    private static boolean withinTwentyFour(int requestedWeekday) {
        if (DEBUG) {
            MyLog.v(CLS_NAME, "in withinTwentyFour");
        }
        final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int todayWeekday = calendar.get(Calendar.DAY_OF_WEEK);
        if (todayWeekday == Calendar.SATURDAY) {
            todayWeekday = 0;
        }
        if (todayWeekday == requestedWeekday) {
            return true;
        }
        if (todayWeekday + 1 != requestedWeekday) {
            return false;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "withinTwentyFour: todayWeekday + 1 == requestedWeekday");
        }
        final int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        final int currentMinute = calendar.get(Calendar.MINUTE);
        if (alarmProcess.getHourOfDay() > currentHour) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "withinTwentyFour: tomorrow: hour > 24");
            }
            return false;
        }
        if (alarmProcess.getHourOfDay() == currentHour && alarmProcess.getMinute() > currentMinute) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "withinTwentyFour: tomorrow: minute > 24");
            }
            return false;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "withinTwentyFour: returning true");
        }
        return true;
    }

    public static boolean setAlarm(Context context, int hour, int minute, String str) {
        final Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, str + " #" + context.getString(R.string.app_name));
        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "ActivityNotFoundException");
                e.printStackTrace();
            }
            return false;
        }
    }

    private static boolean checkTime(String str) {
        if (hasHourOrMinute(str)) {
            hasHourOrMinute = true;
            return true;
        }
        if (hasTime(str)) {
            hasTime = true;
            return true;
        }
        return false;
    }

    private static boolean dissectTime(String str, String delimiter, boolean less) {
        String[] separated = str.split(delimiter);
        if (separated.length == 0) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectTime remove = 0 ");
            }
            return false;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "remove length: " + separated.length);
            MyLog.d(CLS_NAME, "remove0: " + separated[0]);
            MyLog.d(CLS_NAME, "remove1: " + separated[1]);
        }
        final String hoursString = separated[1];
        if (DEBUG) {
            MyLog.d(CLS_NAME, "dissectTime hour: " + hoursString);
        }
        separated = hoursString.split(XMLResultsHandler.SEP_SPACE);
        if (separated.length <= 0) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectTime number = 0 ");
            }
            return false;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "dissect length: " + separated.length);
            MyLog.d(CLS_NAME, "dissect0: " + separated[0]);
            MyLog.d(CLS_NAME, "dissect1: " + separated[1]);
        }
        final String number = separated[1];
        if (DEBUG) {
            MyLog.d(CLS_NAME, "dissectTime number: " + number);
        }
        if (!number.matches("[0-9]+")) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectTime number.matches 0-9+: false" + number);
            }
            return false;
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "dissectTime number.matches 0-9+: " + number);
        }
        try {
            Integer.parseInt(number);
            if (Integer.parseInt(number) <= 0 || Integer.parseInt(number) >= 25) {
                return false;
            }
            if (less) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "dissectTime less: true");
                }
                alarmProcess.setHourOfDay(Integer.parseInt(number) - 1);
            } else {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "dissectTime less: false");
                }
                alarmProcess.setHourOfDay(Integer.parseInt(number));
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectTime: alarmProcess.hourInt: " + alarmProcess.getHourOfDay());
                MyLog.d(CLS_NAME, "dissectTime: alarmProcess.minuteInt: " + alarmProcess.getMinute());
            }
            if (alarmProcess.getHourOfDay() < 25 && alarmProcess.getMinute() < 60) {
                hasAMPM(str);
                return true;
            }
            if (DEBUG) {
                MyLog.w(CLS_NAME, "dissectHourMin: out of bounds");
            }
        } catch (NumberFormatException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "number not numeric");
            }
        }
        return false;
    }

    private static Pair<Integer, Integer> dissectHourAndMinute(String str) {
        final String[] separated = str.trim().split(XMLResultsHandler.SEP_SPACE);
        final int length = separated.length;
        int hour = -1;
        int minute = -1;
        for (int i = 0; i < length; i++) {
            if (separated[i].contains(AlarmHelper.hour)) {
                try {
                    hour = Integer.parseInt(separated[i - 1]);
                } catch (IndexOutOfBoundsException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "HOUR: dissect[i - 1]: IndexOutOfBoundsException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "HOUR: dissect[i - 1]: Exception");
                        e.printStackTrace();
                    }
                }
            }
            if (separated[i].contains(AlarmHelper.minute)) {
                try {
                    minute = Integer.parseInt(separated[i - 1]);
                } catch (IndexOutOfBoundsException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "MINUTE: dissect[i - 1]: IndexOutOfBoundsException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "MINUTE: dissect[i - 1]: Exception");
                        e.printStackTrace();
                    }
                }
            }
        }
        return new Pair<>(Math.max(0, hour), Math.max(0, minute));
    }

    private static void increaseTwelveHours(int pmHour) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "pmHour");
        }
        switch (pmHour) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                alarmProcess.setHourOfDay(pmHour + 12);
                break;
            default:
                break;
        }
    }

    private static boolean hasHourOrMinute(String str) {
        if (str.contains(hour) || str.contains(minute)) {
            final String[] separated = str.trim().split(XMLResultsHandler.SEP_SPACE);
            final int length = separated.length;
            for (int i = 1; i < length; i++) {
                if (separated[i].contains(hour)) {
                    try {
                        String separatedString = separated[i - 1];
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "dissectH: " + separatedString);
                        }
                        try {
                            Integer.parseInt(separatedString);
                            if (!str.contains(minute)) {
                                if (DEBUG) {
                                    MyLog.v(CLS_NAME, "checkHourMinute: hour");
                                }
                                return true;
                            }
                        } catch (NumberFormatException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "Number of hours not numeric");
                            }
                            return false;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "dissectH: -1: IndexOutOfBoundsException");
                            e.printStackTrace();
                        }
                        return false;
                    } catch (Exception e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "dissectH: -1: Exception");
                            e.printStackTrace();
                        }
                        return false;
                    }
                }
                if (separated[i].contains(minute)) {
                    try {
                        String separatedString = separated[i - 1];
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "dissectM: " + separatedString);
                        }
                        try {
                            Integer.parseInt(separatedString);
                            if (DEBUG) {
                                MyLog.v(CLS_NAME, "checkHourMinute: hour and or minute");
                            }
                            return true;
                        } catch (NumberFormatException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "Number of minutes not numeric");
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "dissectM: -1: IndexOutOfBoundsException");
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "dissectM: -1: Exception");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return false;
    }

    private static int dissectHour(String str) {
        final String[] separated = str.trim().split(XMLResultsHandler.SEP_SPACE);
        String separatedString;
        for (int i = 1, length = separated.length; i < length; i++) {
            if (separated[i].contains(hour)) {
                try {
                    separatedString = separated[i - 1];
                    return Integer.parseInt(separatedString);
                } catch (IndexOutOfBoundsException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "HOUR: dissect[i - 1]: IndexOutOfBoundsException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "HOUR: dissect[i - 1]: Exception");
                        e.printStackTrace();
                    }
                }
            }
        }
        return 0;
    }

    private static boolean hasTime(String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "in hasTime");
        }
        final String replace = str.replace(O__CLOCK, "00").replace(O_CLOCK, "00").replace(IN_THE_MORNING, "am").replace(IN_THE_AFTERNOON, "pm").replace(IN_THE_EVENING, "pm");
        if (p3Digits.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "dissectHourMin: threeD");
            }
            Matcher matcher = p3Digits.matcher(replace);
            if (matcher.find()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Matcher found");
                }
                try {
                    String timeDigits = matcher.group(1);
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "timeDigits: " + timeDigits);
                    }
                    String[] separated = timeDigits.split("(?<=\\G.{1})");
                    alarmProcess.setHourOfDay(Integer.parseInt(separated[0]));
                    alarmProcess.setMinute(Integer.parseInt(separated[1] + separated[2]));
                } catch (IndexOutOfBoundsException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "dhm[0]: IndexOutOfBoundsException");
                        e.printStackTrace();
                    }
                    return false;
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "dhm[0]: Exception");
                        e.printStackTrace();
                    }
                    return false;
                }
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectHourMin: hour: " + alarmProcess.getHourOfDay());
                MyLog.d(CLS_NAME, "dissectHourMin: minute: " + alarmProcess.getMinute());
            }
            if (alarmProcess.getHourOfDay() >= 25 || alarmProcess.getMinute() >= 60) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "dissectHourMin: out of bounds");
                }
                return false;
            }
            hasAMPM(replace);
            return true;
        }
        if (p3DigitsGap.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "It matched spaced time! p3DigitsGap");
            }
            Matcher matcher = p3DigitsGap.matcher(replace);
            if (matcher.find()) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "Matcher found");
                }
                try {
                    String timeDigits = matcher.group(1);
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "timeDigits: " + timeDigits);
                    }
                    String[] separated = timeDigits.split("\\s");
                    alarmProcess.setHourOfDay(Integer.parseInt(separated[0]));
                    alarmProcess.setMinute(Integer.parseInt(separated[1]));
                } catch (IndexOutOfBoundsException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "dhm[0]: IndexOutOfBoundsException");
                        e.printStackTrace();
                    }
                    return false;
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "dhm[0]: Exception");
                        e.printStackTrace();
                    }
                    return false;
                }
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectHourMin: hour: " + alarmProcess.getHourOfDay());
                MyLog.d(CLS_NAME, "dissectHourMin: minute: " + alarmProcess.getMinute());
            }
            if (alarmProcess.getHourOfDay() >= 25 || alarmProcess.getMinute() >= 60) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "dissectHourMin: out of bounds");
                }
                return false;
            }
            hasAMPM(replace);
            return true;
        }
        if (p4DigitsGap.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "It matched spaced time! p4DigitsGap");
            }
            Matcher matcher = p4DigitsGap.matcher(replace);
            if (matcher.find()) {
                if (DEBUG) {
                    MyLog.v(CLS_NAME, "Matcher found");
                }
                String timeDigits = matcher.group(1);
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "timeDigits: " + timeDigits);
                }
                String[] separated = timeDigits.split("\\s");
                alarmProcess.setHourOfDay(Integer.parseInt(separated[0]));
                alarmProcess.setMinute(Integer.parseInt(separated[1]));
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectHourMin: hour: " + alarmProcess.getHourOfDay());
                MyLog.d(CLS_NAME, "dissectHourMin: minute: " + alarmProcess.getMinute());
            }
            if (alarmProcess.getHourOfDay() >= 25 || alarmProcess.getMinute() >= 60) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "dissectHourMin: out of bounds");
                }
                return false;
            }
            hasAMPM(replace);
            return true;
        }
        if (p4Digits.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasTime: 4 dddd");
            }
            Matcher matcher = Pattern.compile("\\b(\\d{4})\\b").matcher(replace);
            String timeDigits = "";
            int i = 0;
            while (matcher.find()) {
                i++;
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Matcher found count: " + i + " : " + matcher.group(1));
                }
                timeDigits = matcher.group(1);
            }
            if (DEBUG) {
                MyLog.d(CLS_NAME, "timeDigits: " + timeDigits);
            }
            String[] separated = timeDigits.split("(?<=\\G.{2})");
            alarmProcess.setHourOfDay(Integer.parseInt(separated[0]));
            alarmProcess.setMinute(Integer.parseInt(separated[1]));
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dissectHourMin: hour: " + alarmProcess.getHourOfDay());
                MyLog.d(CLS_NAME, "dissectHourMin: minute: " + alarmProcess.getMinute());
            }
            if (alarmProcess.getHourOfDay() >= 25 || alarmProcess.getMinute() >= 60) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "dissectHourMin: out of bounds");
                }
                return false;
            }
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(FIVE_TO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: five to");
            }
            if (!dissectTime(replace, FIVE_TO, true)) {
                return false;
            }
            alarmProcess.setMinute(55);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(TEN_TO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: ten to");
            }
            if (!dissectTime(replace, TEN_TO, true)) {
                return false;
            }
            alarmProcess.setMinute(50);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(NUMERIC_TEN_TO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: 10 to");
            }
            if (!dissectTime(replace, NUMERIC_TEN_TO, true)) {
                return false;
            }
            alarmProcess.setMinute(50);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(QUARTER_TO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: quarter to");
            }
            if (!dissectTime(replace, QUARTER_TO, true)) {
                return false;
            }
            alarmProcess.setMinute(45);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(TWENTY_TO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: twenty to");
            }
            if (!dissectTime(replace, TWENTY_TO, true)) {
                return false;
            }
            alarmProcess.setMinute(40);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(NUMERIC_TWENTY_FIVE_TO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: 25 to");
            }
            if (!dissectTime(replace, NUMERIC_TWENTY_FIVE_TO, true)) {
                return false;
            }
            alarmProcess.setMinute(35);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(TWENTY_FIVE_TO)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: twenty five to");
            }
            if (!dissectTime(replace, TWENTY_FIVE_TO, true)) {
                return false;
            }
            alarmProcess.setMinute(35);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(HALF_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: half past");
            }
            if (!dissectTime(replace, HALF_PAST, false)) {
                return false;
            }
            alarmProcess.setMinute(30);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(TWENTY_FIVE_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: twenty five past");
            }
            if (!dissectTime(replace, TWENTY_FIVE_PAST, false)) {
                return false;
            }
            alarmProcess.setMinute(25);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(TWENTY_NUMERIC_FIVE_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: twenty five past");
            }
            if (!dissectTime(replace, TWENTY_NUMERIC_FIVE_PAST, false)) {
                return false;
            }
            alarmProcess.setMinute(25);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(NUMERIC_TWENTY_FIVE_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: 25 past");
            }
            if (!dissectTime(replace, NUMERIC_TWENTY_FIVE_PAST, false)) {
                return false;
            }
            alarmProcess.setMinute(25);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(TWENTY_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: twenty past");
            }
            if (!dissectTime(replace, TWENTY_PAST, false)) {
                return false;
            }
            alarmProcess.setMinute(20);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(NUMERIC_TWENTY_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: twenty past");
            }
            if (!dissectTime(replace, NUMERIC_TWENTY_PAST, false)) {
                return false;
            }
            alarmProcess.setMinute(20);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(QUARTER_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: quarter past");
            }
            if (!dissectTime(replace, QUARTER_PAST, false)) {
                return false;
            }
            alarmProcess.setMinute(15);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(TEN_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: ten past");
            }
            if (!dissectTime(replace, TEN_PAST, false)) {
                return false;
            }
            alarmProcess.setMinute(10);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(NUMERIC_TEN_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: 10 past");
            }
            if (!dissectTime(replace, NUMERIC_TEN_PAST, false)) {
                return false;
            }
            alarmProcess.setMinute(10);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(FIVE_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: five past");
            }
            if (!dissectTime(replace, FIVE_PAST, false)) {
                return false;
            }
            alarmProcess.setMinute(5);
            hasAMPM(replace);
            return true;
        }
        if (replace.contains(NUMERIC_FIVE_PAST)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: 5 past");
            }
            if (!dissectTime(replace, NUMERIC_FIVE_PAST, false)) {
                return false;
            }
            alarmProcess.setMinute(5);
            hasAMPM(replace);
            return true;
        }
        if (p1DigitAM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitAM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)\\sam\\b").matcher(replace);
            if (matcher.find()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Matcher found count: " + matcher.group(1));
                }
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitAM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitAM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)\\sam\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitPM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitPM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)\\spm\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitPM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitPM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)\\spm\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitASM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitASM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)\\sa\\sm\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitASM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitASM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)\\sa\\sm\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitPSM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitPSM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)\\sp\\sm\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitPSM.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitPSM");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)\\sp\\sm\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitAMNS.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitAMNS");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)am\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitAMNS.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitAMNS");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)am\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitPMNS.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitPMNS");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)pm\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitPMNS.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitPMNS");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)pm\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitPMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitPMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)[p.m.]\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitPMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitPMDOT");
            }
            Matcher matcher18 = Pattern.compile("\\b(\\d\\d)[p.m.]\\b").matcher(replace);
            if (matcher18.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher18.group(1)));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher18.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitAMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitAMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)[a.m.]\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitAMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitAMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)[a.m.]\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitSPMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitSPMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)\\s[p.m.]\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitSPMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitSPMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)\\s[p.m.]\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        if (p1DigitSAMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p1DigitSAMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d)\\s[a.m.]\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        if (p2DigitSAMDOT.matcher(replace).matches()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "hasTime: p2DigitSAMDOT");
            }
            Matcher matcher = Pattern.compile("\\b(\\d\\d)\\s[a.m.]\\b").matcher(replace);
            if (matcher.find()) {
                alarmProcess.setHourOfDay(Integer.parseInt(matcher.group(1)));
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "group1: " + matcher.group(1));
                }
            }
            hasAMPM(replace);
            return true;
        }
        return false;
    }

    private static int dissectMinute(String str) {
        final String[] separated = str.trim().split(XMLResultsHandler.SEP_SPACE);
        String separatedString;
        for (int i = 1, length = separated.length; i < length; i++) {
            if (separated[i].contains(minute)) {
                try {
                    separatedString = separated[i - 1];
                    return Integer.parseInt(separatedString);
                } catch (IndexOutOfBoundsException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "MINUTE: dissect[i - 1]: IndexOutOfBoundsException");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "MINUTE: dissect[i - 1]: Exception");
                        e.printStackTrace();
                    }
                }
            }
        }
        return 0;
    }

    private static String getTimes(String str) {
        String[] separated = {""};
        if (pCalled.matcher(str).matches()) {
            separated = str.split(called);
        } else if (pCall.matcher(str).matches()) {
            separated = str.split(call);
        } else if (pCalls.matcher(str).matches()) {
            separated = str.split(calls);
        } else if (pNamed.matcher(str).matches()) {
            separated = str.split(named);
        } else if (pName.matcher(str).matches()) {
            separated = str.split(name);
        } else if (pNames.matcher(str).matches()) {
            separated = str.split(names);
        } else if (pCold.matcher(str).matches()) {
            separated = str.split(cold);
        }
        return separated.length > 1 ? separated[1] : "";
    }

    private static boolean hasAMPM(String str) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "in hasAMPM");
        }
        alarmProcess.setAM(false);
        alarmProcess.setPM(false);
        if (pAM.matcher(str).matches() && pPM.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "hasAMPM: BOTH");
            }
            return false;
        }
        if (pAM.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pAM: AM");
            }
            alarmProcess.setAM(true);
            return true;
        }
        if (pPM.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pPM: PM");
            }
            alarmProcess.setPM(true);
            return true;
        }
        if (pPMDOT.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pPMDOT: PM");
            }
            alarmProcess.setPM(true);
            return true;
        }
        if (pPMS.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pPMS: PM");
            }
            alarmProcess.setPM(true);
            return true;
        }
        if (pPM1D.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pPM1D: PM");
            }
            alarmProcess.setPM(true);
            return true;
        }
        if (pPM2D.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pPM2D: PM");
            }
            alarmProcess.setPM(true);
            return true;
        }
        if (pAMDOT.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pAMDOT : AM");
            }
            alarmProcess.setAM(true);
            return true;
        }
        if (pAMS.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pAMS: AM");
            }
            alarmProcess.setAM(true);
            return true;
        }
        if (pAM1D.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pAM1D: AM");
            }
            alarmProcess.setAM(true);
            return true;
        }
        if (pAM2D.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pAM2D: AM");
            }
            alarmProcess.setAM(true);
            return true;
        }
        if (pAM1DDOT.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pAM1DDOT: AM");
            }
            alarmProcess.setAM(true);
            return true;
        }
        if (pAM2DDOT.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pAM2DDOT: AM");
            }
            alarmProcess.setAM(true);
            return true;
        }
        if (pPM1DDOT.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pPM1DDOT: PM");
            }
            alarmProcess.setPM(true);
            return true;
        }
        if (pPM2DDOT.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: pPM2DDOT: PM");
            }
            alarmProcess.setPM(true);
            return true;
        }
        if (str.contains(_MORNING)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: morning");
            }
            alarmProcess.setAM(true);
            return false;
        }
        if (str.contains(_AFTERNOON)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: afternoon");
            }
            alarmProcess.setPM(true);
            return false;
        }
        if (str.contains(_EVENING)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: evening");
            }
            alarmProcess.setPM(true);
            return false;
        }
        if (str.contains(_NIGHT)) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "hasAMPM: night");
            }
            alarmProcess.setPM(true);
        }
        return false;
    }

    private static @NonNull Pair<Boolean, Integer> isWeekday(String str) {
        boolean isWeekday = true;
        if (DEBUG) {
            MyLog.i(CLS_NAME, "in isWeekday");
        }
        if (pMONDAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pMonday");
            }
            alarmProcess.setWeekday(Calendar.MONDAY);
        } else if (pTUESDAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pTuesday");
            }
            alarmProcess.setWeekday(Calendar.TUESDAY);
        } else if (pWEDNESDAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pWednesday");
            }
            alarmProcess.setWeekday(Calendar.WEDNESDAY);
        } else if (pTHURSDAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pThursday");
            }
            alarmProcess.setWeekday(Calendar.THURSDAY);
        } else if (pFRIDAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pFriday");
            }
            alarmProcess.setWeekday(Calendar.FRIDAY);
        } else if (pSATURDAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pSaturday");
            }
            alarmProcess.setWeekday(Calendar.SATURDAY);
        } else if (pSUNDAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pSunday");
            }
            alarmProcess.setWeekday(Calendar.SUNDAY);
        } else if (pTODAY.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pToday");
            }
            alarmProcess.setWeekday(Calendar.getInstance(TimeZone.getDefault()).get(Calendar.DAY_OF_WEEK));
        } else if (pTOMORROW.matcher(str).matches()) {
            if (DEBUG) {
                MyLog.v(CLS_NAME, "isWeekday: pTomorrow");
            }
            alarmProcess.setWeekday((Calendar.getInstance(TimeZone.getDefault()).get(Calendar.DAY_OF_WEEK) + 1) % Calendar.SATURDAY + Calendar.SUNDAY);
        } else {
            isWeekday = false;
        }
        return new Pair<>(isWeekday, alarmProcess.getWeekday());
    }
}
