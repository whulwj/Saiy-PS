package ai.saiy.android.command.call;

import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Redial implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object redial;

    public Redial(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.redial = new Redial_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.redial = new Redial_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.redial = new Redial_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Redial_en) redial).detectCallable();
            case ENGLISH_US:
                return ((Redial_en) redial).detectCallable();
            default:
                return ((Redial_en) redial).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
