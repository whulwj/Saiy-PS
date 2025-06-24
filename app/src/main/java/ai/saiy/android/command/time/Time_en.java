package ai.saiy.android.command.time;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;

public class Time_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Time_en.class.getSimpleName();
    private static String whats_the_time;
    private static String what_is_the_time;
    private static String what_the_time_is;
    private static String what_time_is_it;
    private static String what_time_it_is;
    private static String tell_me_the_time;
    private static String time;
    private static String in;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Time_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (whats_the_time == null) {
            if (DEBUG) {
               MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
           MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static ArrayList<String> sortTime(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();
        ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        if (time == null) {
            if (DEBUG) {
               MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
           MyLog.i(CLS_NAME, "strings initialised");
        }
        String vdLower;
        for (String voiceDatum : voiceData) {
            vdLower = voiceDatum.toLowerCase(locale).trim();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "examining: " + vdLower);
            }
            if (vdLower.contains(time + Constants.SEP_SPACE)) {
                String[] separated = vdLower.split(time + Constants.SEP_SPACE);
                if (separated.length > 1) {
                    String second = separated[1].trim();
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "examining: second " + second);
                    }

                    if (second.matches(".*\\b" + in + "\\b.*")) {
                        separated = second.split("\\b" + in + "\\b", 2);
                        if (separated.length > 1) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "examining: separated " + separated[1]);
                            }
                            second = separated[1].trim();
                            if (ai.saiy.android.utils.UtilsString.notNaked(second)) {
                                toReturn.add(second);
                            }
                        }
                    }
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        whats_the_time = sr.getString(R.string.whats_the_time);
        what_is_the_time = sr.getString(R.string.what_is_the_time);
        what_the_time_is = sr.getString(R.string.what_the_time_is);
        what_time_is_it = sr.getString(R.string.what_time_is_it);
        what_time_it_is = sr.getString(R.string.what_time_it_is);
        tell_me_the_time = sr.getString(R.string.tell_me_the_time);
        time = sr.getString(R.string.time);
        in = sr.getString(R.string.in);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            int size = voiceData.size();
            String vdLower;
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.startsWith(whats_the_time) || vdLower.startsWith(what_is_the_time) || vdLower.startsWith(what_time_is_it) || vdLower.endsWith(whats_the_time) || vdLower.endsWith(what_is_the_time) || vdLower.endsWith(what_time_is_it) || vdLower.endsWith(what_the_time_is) || vdLower.endsWith(what_time_it_is) || vdLower.contains(tell_me_the_time) || vdLower.matches(time) || vdLower.startsWith(time + Constants.SEP_SPACE + in)) {
                    toReturn.add(new Pair<>(CC.COMMAND_TIME, confidence[i]));
                }
            }
        }
        if (DEBUG) {
           MyLog.i(CLS_NAME, "Time: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
