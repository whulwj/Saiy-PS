package ai.saiy.android.command.clipboard;

import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Clipboard implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object clipboard;

    public Clipboard(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.clipboard = new Clipboard_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.clipboard = new Clipboard_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.clipboard = new Clipboard_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (this.sl) {
            case ENGLISH:
                return ((Clipboard_en) this.clipboard).detectCallable();
            case ENGLISH_US:
                return ((Clipboard_en) this.clipboard).detectCallable();
            default:
                return ((Clipboard_en) this.clipboard).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
