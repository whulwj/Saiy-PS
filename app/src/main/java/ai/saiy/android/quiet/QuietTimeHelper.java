package ai.saiy.android.quiet;

import android.content.Context;

import com.google.gson.JsonSyntaxException;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsParcelable;

public class QuietTimeHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = QuietTimeHelper.class.getSimpleName();

    private static QuietTime defaultQuietTime() {
        return new QuietTime(21, 11, 9, 11);
    }

    public static void save(Context context, QuietTime quietTime) {
        if (quietTime == null) {
            quietTime = defaultQuietTime();
        }
        String base64String = UtilsParcelable.parcelable2String(quietTime);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "save: base64String: " + base64String);
        }
        SPH.setQuietTimes(context, base64String);
    }

    public static boolean canProceed(Context context) {
        QuietTime quietTime = getQuietTimes(context);
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        int currentCombined = calendar.get(Calendar.MINUTE) + (calendar.get(Calendar.HOUR_OF_DAY) * 60);
        int qtStartCombined = (quietTime.getStartHour() * 60) + quietTime.getStartMinute();
        int qtEndCombined = quietTime.getEndMinute() + (quietTime.getEndHour() * 60);
        if (qtStartCombined > qtEndCombined) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "canProceed - qtStartCombined > qtEndCombined");
            }
            if (currentCombined < qtStartCombined && currentCombined > qtEndCombined) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "canProceed true: currentCombined < qtStartCombined && currentCombined > qtEndCombined");
                }
                return true;
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "canProceed - qtStartCombined < qtEndCombined");
            }
            if (currentCombined < qtStartCombined || currentCombined > qtEndCombined) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "canProceed true: currentCombined < qtStartCombined || currentCombined > qtEndCombined");
                }
                return true;
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "canProceed false");
        }
        return false;
    }

    public static QuietTime getQuietTimes(Context context) {
        if (haveQuietTimes(context)) {
            try {
                QuietTime quietTime = UtilsParcelable.unmarshall(SPH.getQuietTimes(context), QuietTime.CREATOR);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "profile: " + quietTime);
                }
                return quietTime;
            } catch (JsonSyntaxException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "JsonSyntaxException");
                    e.printStackTrace();
                }
            } catch (NullPointerException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "NullPointerException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Exception");
                    e.printStackTrace();
                }
            }
        }
        return defaultQuietTime();
    }

    private static boolean haveQuietTimes(Context context) {
        return SPH.getQuietTimes(context) != null;
    }
}
