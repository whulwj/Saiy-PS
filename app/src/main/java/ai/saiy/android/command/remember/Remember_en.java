package ai.saiy.android.command.remember;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public final class Remember_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Remember_en.class.getSimpleName();
    private static String remember_this_;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Remember_en(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (remember_this_ == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static ArrayList<String> fetchTarget(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();
        if (remember_this_ == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        for (String voiceDatum : voiceData) {
            String vdLower = voiceDatum.toLowerCase(locale).trim();
            if (vdLower.startsWith(remember_this_)) {
                String[] split = vdLower.split(remember_this_, 2);
                if (split.length > 1) {
                    toReturn.add(split[1].trim());
                }
            }
        }
        if (!toReturn.isEmpty()) {
            final LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>(toReturn);
            toReturn.clear();
            toReturn.addAll(linkedHashSet);
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }

    private static void initStrings(@NonNull ai.saiy.android.localisation.SaiyResources sr) {
        remember_this_ = sr.getString(R.string.remember_this_);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                if (voiceData.get(i).toLowerCase(locale).trim().startsWith(remember_this_)) {
                    toReturn.add(new Pair<>(CC.COMMAND_REMEMBER, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "remember: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
