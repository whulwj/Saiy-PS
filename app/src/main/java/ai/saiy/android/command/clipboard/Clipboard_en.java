package ai.saiy.android.command.clipboard;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public class Clipboard_en {
    private static String read;
    private static String speak;
    private static String say;
    private static String announce;
    private static String clipboard;
    private static String clip_board;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Clipboard_en.class.getSimpleName();

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Clipboard_en(ai.saiy.android.localisation.SaiyResources aVar, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (read == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(aVar);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        read = sr.getString(R.string.read);
        speak = sr.getString(R.string.speak);
        say = sr.getString(R.string.say);
        announce = sr.getString(R.string.announce);
        clipboard = sr.getString(R.string.clipboard);
        clip_board = sr.getString(R.string.clip_board);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            String vdLower;
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if ((vdLower.startsWith(read) || vdLower.startsWith(speak) || vdLower.startsWith(say) || vdLower.startsWith(announce)) && (vdLower.contains(clip_board) || vdLower.contains(clipboard))) {
                    toReturn.add(new Pair<>(CC.COMMAND_CLIPBOARD, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "clipboard: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
