package ai.saiy.android.command.coin;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;

public final class Coin_en {
    private static String coin;
    private static String flip;
    private static String toss;
    private static String heads;
    private static String or;
    private static String tails;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Coin_en.class.getSimpleName();

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Coin_en(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (coin == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    private static void initStrings(@NonNull ai.saiy.android.localisation.SaiyResources sr) {
        coin = sr.getString(R.string.coin);
        flip = sr.getString(R.string.flip);
        toss = sr.getString(R.string.toss);
        heads = sr.getString(R.string.heads);
        tails = sr.getString(R.string.tails);
        or = sr.getString(R.string.or);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if ((vdLower.startsWith(toss) || vdLower.startsWith(flip)) && vdLower.contains(coin)) {
                    toReturn.add(new Pair<>(CC.COMMAND_COIN, confidence[i]));
                } else if (vdLower.matches(heads + Constants.SEP_SPACE + or + Constants.SEP_SPACE + tails)) {
                    toReturn.add(new Pair<>(CC.COMMAND_COIN, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "coin: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
