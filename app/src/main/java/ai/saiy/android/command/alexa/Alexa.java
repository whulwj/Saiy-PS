package ai.saiy.android.command.alexa;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Alexa implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object alexa;

    public Alexa(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Alexa(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.alexa = new Alexa_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.alexa = new Alexa_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.alexa = new Alexa_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Alexa_en) alexa).detectCallable();
            case ENGLISH_US:
                return ((Alexa_en) alexa).detectCallable();
            default:
                return ((Alexa_en) alexa).detectCallable();
        }
    }

    public ArrayList<String> sort(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Alexa_en.sortAlexa(context, voiceData, sl);
            case ENGLISH_US:
                return Alexa_en.sortAlexa(context, voiceData, sl);
            default:
                return Alexa_en.sortAlexa(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
