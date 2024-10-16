package ai.saiy.android.command.coin;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public final class Coin implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object coin;

    public Coin(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.coin = new Coin_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.coin = new Coin_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.coin = new Coin_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Coin_en) coin).detectCallable();
            case ENGLISH_US:
                return ((Coin_en) coin).detectCallable();
            default:
                return ((Coin_en) coin).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
