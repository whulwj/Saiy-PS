package ai.saiy.android.command.ability;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Restart implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object restart;

    public Restart(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.restart = new Restart_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.restart = new Restart_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.restart = new Restart_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Restart_en) restart).detectCallable();
            case ENGLISH_US:
                return ((Restart_en) restart).detectCallable();
            default:
                return ((Restart_en) restart).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
