package ai.saiy.android.command.timer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.AlarmClock;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public final class TimerHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = TimerHelper.class.getSimpleName();

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
    private static String minute;
    private static String hour;
    private static String SECOND;
    private static String TO_MINUTES;
    private static String FOR_MINUTES;

    private static int dissectTime(CommandTimerValue commandTimerValue, String str) {
        int runningTotal = 0;
        final String[] separated = str.trim().split(Constants.SEP_SPACE);
        for (int i = 0; i < separated.length; i++) {
            if (separated[i].contains(hour)) {
                try {
                    String dissect = separated[i - 1];
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "dissect: " + dissect);
                    }
                    final int hours = Integer.parseInt(dissect);
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "hourNumber: " + hours);
                    }
                    commandTimerValue.setHour(hours);
                    runningTotal += hours * 60 * 60;
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "hours: runningTotal: " + runningTotal);
                    }
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "Number of hours not numeric");
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "disecth: -1");
                    }
                }
            }
            if (separated[i].contains(minute)) {
                try {
                    final String dissectM = separated[i - 1];
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "dissectM: " + dissectM);
                    }
                    final int minuteNumber = Integer.parseInt(dissectM);
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "minuteNumber: " + minuteNumber);
                    }
                    commandTimerValue.setMinute(minuteNumber);
                    runningTotal += minuteNumber * 60;
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "Minutes: runningTotal: " + runningTotal);
                    }
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "Number of minutes not numeric");
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "dissectM: -1");
                    }
                }
            }
            if (separated[i].contains(SECOND)) {
                try {
                    final String dissectS = separated[i - 1];
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "dissectS: " + dissectS);
                    }
                    final int secondNumber = Integer.parseInt(dissectS);
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "secondNumber: " + secondNumber);
                    }
                    commandTimerValue.setSecond(secondNumber);
                    runningTotal += secondNumber;
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "Seconds: runningTotal: " + runningTotal);
                    }
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "Number of seconds not numeric");
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "dissectS: -1");
                    }
                }
            }
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "Returning - runningTotal: " + runningTotal);
        }
        return runningTotal;
    }

    public static @NonNull CommandTimerValue resolveAlarm(Context context, ArrayList<String> timers, Locale locale) {
        final CommandTimerValue commandTimerValue = new CommandTimerValue();
        if (minute == null || pCalled == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "resolveAlarm: initialising strings");
            }
            initStrings(context);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "resolveAlarm: strings initialised");
        }
        for (int i = 0; i < timers.size(); i++) {
            String timerTime = timers.get(i);
            if (DEBUG) {
                MyLog.d(CLS_NAME, "timerTime: " + timerTime);
            }
            timerTime = timerTime.replace(TO_MINUTES, "2 " + minute).replace(FOR_MINUTES, "4 " + minute);
            final int runningTotal = dissectTime(commandTimerValue, timerTime);
            if (runningTotal > 0) {
                commandTimerValue.setValidness(true);
                commandTimerValue.setRunningTotal(runningTotal);
                commandTimerValue.setCallee(getCallee(timerTime, locale));
                return commandTimerValue;
            }
        }
        commandTimerValue.setValidness(false);
        return commandTimerValue;
    }

    private static String getCallee(String str, Locale locale) {
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
        if (separated.length > 1) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "cs length: " + separated.length);
                MyLog.d(CLS_NAME, "cs0: " + separated[0]);
                MyLog.d(CLS_NAME, "cs1: " + separated[1]);
            }
            if (!separated[1].trim().isEmpty()) {
                return ai.saiy.android.utils.UtilsString.convertProperCase(separated[1].trim(), locale);
            }
        }
        return "";
    }

    private static void initStrings(Context context) {
        minute = context.getString(R.string.minute);
        hour = context.getString(R.string.hour);
        SECOND = context.getString(R.string.SECOND);
        TO_MINUTES = context.getString(R.string.TO_MINUTES);
        FOR_MINUTES = context.getString(R.string.FOR_MINUTES);
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
    }

    public static boolean setTimer(Context context, int length, String callee) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setTimer");
        }
        final Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.putExtra(AlarmClock.EXTRA_LENGTH, length);
        } else {
            intent.putExtra("android.intent.extra.alarm.LENGTH", length);
        }
        if (UtilsString.notNaked(callee)) {
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, callee + " #" + context.getString(R.string.app_name));
        } else {
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, "#" + context.getString(R.string.app_name));
        }
        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "ActivityNotFoundException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }
        return false;
    }
}
