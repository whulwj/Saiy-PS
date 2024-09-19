package ai.saiy.android.command.maintenance;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public class Shutdown_en {
    private static String shutdown;
    private static String shut_down;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Shutdown_en.class.getSimpleName();

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Shutdown_en(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (shutdown == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    private static void initStrings(@NonNull ai.saiy.android.localisation.SaiyResources sr) {
        shutdown = sr.getString(R.string.shutdown);
        shut_down = sr.getString(R.string.shut_down);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.matches(shutdown) || vdLower.matches(shut_down)) {
                    toReturn.add(new Pair<>(CC.COMMAND_SHUTDOWN, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "shutdown: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
