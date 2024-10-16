package ai.saiy.android.command.date;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public final class Date_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Date_en.class.getSimpleName();
    private static String whats_the_date;
    private static String what_is_the_date;
    private static String what_the_date_is;
    private static String what_date_is_it;
    private static String tell_me_the_date;
    private static String date;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Date_en(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (whats_the_date == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    private static void initStrings(@NonNull ai.saiy.android.localisation.SaiyResources sr) {
        whats_the_date = sr.getString(R.string.whats_the_date);
        what_is_the_date = sr.getString(R.string.what_is_the_date);
        what_the_date_is = sr.getString(R.string.what_the_date_is);
        what_date_is_it = sr.getString(R.string.what_date_is_it);
        tell_me_the_date = sr.getString(R.string.tell_me_the_date);
        date = sr.getString(R.string.date);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(this.voiceData) && ai.saiy.android.utils.UtilsList.notNaked(this.confidence) && this.voiceData.size() == this.confidence.length) {
            final Locale locale = this.sl.getLocale();
            final int size = this.voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = this.voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.startsWith(whats_the_date) || vdLower.startsWith(what_is_the_date) || vdLower.startsWith(what_date_is_it) || vdLower.endsWith(whats_the_date) || vdLower.endsWith(what_is_the_date) || vdLower.endsWith(what_date_is_it) || vdLower.endsWith(what_the_date_is) || vdLower.contains(tell_me_the_date) || vdLower.matches(date)) {
                    toReturn.add(new Pair<>(CC.COMMAND_DATE, this.confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "Date: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
