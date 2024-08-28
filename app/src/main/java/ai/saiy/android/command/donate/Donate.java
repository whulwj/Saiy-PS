package ai.saiy.android.command.donate;

import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Donate implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object donate;

    public Donate(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.donate = new Donate_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.donate = new Donate_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.donate = new Donate_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Donate_en) donate).detectCallable();
            case ENGLISH_US:
                return ((Donate_en) donate).detectCallable();
            default:
                return ((Donate_en) donate).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
