package ai.saiy.android.command.timer;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public final class Timer_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Timer_en.class.getSimpleName();

    private static String timer;
    private static String set;
    private static String word_new;
    private static String create;
    private static String schedule;
    private static String countdown;
    private static String count_down;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Timer_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (timer == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static ArrayList<String> sortTimer(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();
        final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        if (timer == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        final String hour = sr.getString(R.string.hour);
        final String minute = sr.getString(R.string.minute);
        final String second = sr.getString(R.string.second);
        sr.reset();
        for (String voiceDatum : voiceData) {
            String vdLower = voiceDatum.toLowerCase(locale).trim();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "examining: " + vdLower);
            }
            if (vdLower.startsWith(set) || vdLower.startsWith(word_new) || vdLower.startsWith(create) || vdLower.startsWith(schedule)) {
                if (vdLower.contains(timer) || vdLower.contains(countdown) || vdLower.contains(count_down)) {
                    if (vdLower.contains(hour) || vdLower.contains(minute) || vdLower.contains(second)) {
                        toReturn.add(vdLower);
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
        timer = sr.getString(R.string.timer);
        set = sr.getString(R.string.set);
        word_new = sr.getString(R.string.word_new);
        create = sr.getString(R.string.create);
        schedule = sr.getString(R.string.schedule);
        countdown = sr.getString(R.string.countdown);
        count_down = sr.getString(R.string.count_down);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if ((vdLower.startsWith(set) || vdLower.startsWith(word_new) || vdLower.startsWith(create) || vdLower.startsWith(schedule)) && (vdLower.contains(timer) || vdLower.contains(countdown) || vdLower.contains(count_down))) {
                    toReturn.add(new Pair<>(CC.COMMAND_TIMER, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "timer: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
