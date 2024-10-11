package ai.saiy.android.command.card;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public final class Card implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object card;

    public Card(@NonNull ai.saiy.android.localisation.SaiyResources aVar, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.card = new Card_en(aVar, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.card = new Card_en(aVar, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.card = new Card_en(aVar, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Card_en) card).detectCallable();
            case ENGLISH_US:
                return ((Card_en) card).detectCallable();
            default:
                return ((Card_en) card).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
