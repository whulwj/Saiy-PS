package ai.saiy.android.command.toast;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public final class Toast_en {
    private static String toast;
    private static String clipboard;
    private static String clip_board;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Toast_en.class.getSimpleName();
    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Toast_en(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (toast == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        toast = sr.getString(R.string.toast);
        clipboard = sr.getString(R.string.clipboard);
        clip_board = sr.getString(R.string.clip_board);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.startsWith(toast) && (vdLower.contains(clip_board) || vdLower.contains(clipboard))) {
                    toReturn.add(new Pair<>(CC.COMMAND_TOAST, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "toast: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
