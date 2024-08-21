package ai.saiy.android.command.somersault;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class Somersault_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Somersault_en.class.getSimpleName();
    private static String forward;
    private static String backward;
    private static String word_do;
    private static String somersault;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Somersault_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (forward == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static @NonNull ai.saiy.android.command.orientation.CommandOrientationValues detectSomersault(android.content.Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ai.saiy.android.command.orientation.CommandOrientationValues commandOrientationValues = new ai.saiy.android.command.orientation.CommandOrientationValues();
        commandOrientationValues.setDescription("");
        commandOrientationValues.setType(ai.saiy.android.command.orientation.CommandOrientationValues.Type.UNKNOWN);
        if (forward == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
            sr.reset();
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }

        for (String voiceDatum : voiceData) {
            String vdLower = voiceDatum.toLowerCase(locale).trim();
            if (vdLower.startsWith(somersault)) {
                if (!vdLower.contains(forward) && vdLower.contains(backward)) {
                    commandOrientationValues.setDescription(backward);
                    commandOrientationValues.setType(ai.saiy.android.command.orientation.CommandOrientationValues.Type.SOMERSAULT_BACKWARD);
                } else {
                    commandOrientationValues.setDescription(forward);
                    commandOrientationValues.setType(ai.saiy.android.command.orientation.CommandOrientationValues.Type.SOMERSAULT_FORWARD);
                }
                break;
            } else if (vdLower.startsWith(forward) || vdLower.startsWith(backward) || vdLower.startsWith(word_do)) {
                if (!vdLower.contains(somersault)) {
                    continue;
                }
                if (!vdLower.contains(forward) && vdLower.contains(backward)) {
                    commandOrientationValues.setDescription(backward);
                    commandOrientationValues.setType(ai.saiy.android.command.orientation.CommandOrientationValues.Type.SOMERSAULT_BACKWARD);
                } else {
                    commandOrientationValues.setDescription(forward);
                    commandOrientationValues.setType(ai.saiy.android.command.orientation.CommandOrientationValues.Type.SOMERSAULT_FORWARD);
                }
                break;
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return commandOrientationValues;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        forward = sr.getString(R.string.forward);
        backward = sr.getString(R.string.backward);
        word_do = sr.getString(R.string.word_do);
        somersault = sr.getString(R.string.somersault);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.startsWith(somersault) || ((vdLower.startsWith(forward) || vdLower.startsWith(backward) || vdLower.startsWith(word_do)) && vdLower.contains(somersault))) {
                    toReturn.add(new Pair<>(CC.COMMAND_SOMERSAULT, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "somersault: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
