package ai.saiy.android.command.note;

import android.content.Context;
import android.util.Pair;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public class Note_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Note_en.class.getSimpleName();
    private static String note;
    private static String voice;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Note_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (note == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static CommandNoteValues sortNote(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final CommandNoteValues commandNoteValues = new CommandNoteValues();
        commandNoteValues.setType(CommandNoteValues.Type.UNKNOWN);
        if (note == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
            sr.reset();
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        String vdLower;
        for (String vdDatum : voiceData) {
            vdLower = vdDatum.toLowerCase(locale).trim();
            if (vdLower.endsWith(voice + XMLResultsHandler.SEP_SPACE + note)) {
                commandNoteValues.setType(CommandNoteValues.Type.VOICE_NOTE);
                break;
            }
            if (vdLower.endsWith(note)) {
                commandNoteValues.setType(CommandNoteValues.Type.NOTE);
                break;
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return commandNoteValues;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        note = sr.getString(R.string.note);
        voice = sr.getString(R.string.voice);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                if (voiceData.get(i).toLowerCase(locale).trim().endsWith(note)) {
                    toReturn.add(new Pair<>(CC.COMMAND_NOTE, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "note: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
