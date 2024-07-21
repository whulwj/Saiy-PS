package ai.saiy.android.command.call;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class Redial_en {
    private static String redial;
    private static String re_dial;
    private static String free_dial;
    private static String read_dial;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Redial_en.class.getSimpleName();
    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Redial_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (redial == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        redial = sr.getString(R.string.redial);
        re_dial = sr.getString(R.string.re_dial);
        free_dial = sr.getString(R.string.free_dial);
        read_dial = sr.getString(R.string.read_dial);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            String vdLower;
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.startsWith(redial) || vdLower.startsWith(re_dial) || vdLower.startsWith(read_dial) || vdLower.startsWith(free_dial)) {
                    toReturn.add(new Pair<>(CC.COMMAND_REDIAL, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "redial: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
