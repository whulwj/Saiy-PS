package ai.saiy.android.command.easter_egg;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public final class EasterEgg implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object easterEgg;

    public EasterEgg(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.easterEgg = new EasterEgg_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.easterEgg = new EasterEgg_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.easterEgg = new EasterEgg_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((EasterEgg_en) easterEgg).detectCallable();
            case ENGLISH_US:
                return ((EasterEgg_en) easterEgg).detectCallable();
            default:
                return ((EasterEgg_en) easterEgg).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
