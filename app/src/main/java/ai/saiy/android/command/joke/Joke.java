package ai.saiy.android.command.joke;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public final class Joke implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object joke;

    public Joke(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.joke = new Joke_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.joke = new Joke_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.joke = new Joke_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Joke_en) joke).detectCallable();
            case ENGLISH_US:
                return ((Joke_en) joke).detectCallable();
            default:
                return ((Joke_en) joke).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
