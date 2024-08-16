package ai.saiy.android.command.application.foreground;

import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Foreground implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object foreground;

    public Foreground(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.foreground = new Foreground_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.foreground = new Foreground_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.foreground = new Foreground_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Foreground_en) foreground).detectCallable();
            case ENGLISH_US:
                return ((Foreground_en) foreground).detectCallable();
            default:
                return ((Foreground_en) foreground).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
