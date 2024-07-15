package ai.saiy.android.command.note;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Note implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object note;

    public Note(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Note(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.note = new Note_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.note = new Note_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.note = new Note_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public CommandNoteValues sort(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Note_en.sortNote(context, voiceData, sl);
            case ENGLISH_US:
                return Note_en.sortNote(context, voiceData, sl);
            default:
                return Note_en.sortNote(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Note_en) note).detectCallable();
            case ENGLISH_US:
                return ((Note_en) note).detectCallable();
            default:
                return ((Note_en) note).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
