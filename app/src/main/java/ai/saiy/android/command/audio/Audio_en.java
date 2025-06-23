package ai.saiy.android.command.audio;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class Audio_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Audio_en.class.getSimpleName();

    private static String silent;
    private static String vibrate;
    private static String normal;
    private static String sound;
    private static String audio;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Audio_en(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (silent == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static CommandAudioValues sortAudio(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final CommandAudioValues commandAudioValues = new CommandAudioValues();
        commandAudioValues.setDescription("");
        commandAudioValues.setType(CommandAudioValues.Type.UNKNOWN);
        if (silent == null) {
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
            if (vdLower.contains(sound) || vdLower.contains(audio)) {
                if (vdLower.contains(vibrate)) {
                    commandAudioValues.setDescription(vibrate);
                    commandAudioValues.setType(CommandAudioValues.Type.VIBRATE);
                    break;
                }
                if (vdLower.contains(silent)) {
                    commandAudioValues.setDescription(silent);
                    commandAudioValues.setType(CommandAudioValues.Type.SILENT);
                    break;
                }
                if (vdLower.contains(normal)) {
                    commandAudioValues.setDescription(normal);
                    commandAudioValues.setType(CommandAudioValues.Type.NORMAL);
                    break;
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return commandAudioValues;
    }

    private static void initStrings(@NonNull ai.saiy.android.localisation.SaiyResources sr) {
        silent = sr.getString(R.string.silent);
        vibrate = sr.getString(R.string.vibrate);
        normal = sr.getString(R.string.normal);
        sound = sr.getString(R.string.sound);
        audio = sr.getString(R.string.audio);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if ((vdLower.contains(Audio_en.sound) || vdLower.contains(audio)) && (vdLower.contains(silent) || vdLower.contains(vibrate) || vdLower.contains(normal))) {
                    toReturn.add(new Pair<>(CC.COMMAND_AUDIO, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "audio: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
