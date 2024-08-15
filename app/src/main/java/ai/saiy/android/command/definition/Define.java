package ai.saiy.android.command.definition;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Define implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object define;

    public Define(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Define(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.define = new Define_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.define = new Define_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.define = new Define_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Define_en) define).detectCallable();
            case ENGLISH_US:
                return ((Define_en) define).detectCallable();
            default:
                return ((Define_en) define).detectCallable();
        }
    }

    public ArrayList<String> sortWord(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Define_en.sortWord(context, voiceData, sl);
            case ENGLISH_US:
                return Define_en.sortWord(context, voiceData, sl);
            default:
                return Define_en.sortWord(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
