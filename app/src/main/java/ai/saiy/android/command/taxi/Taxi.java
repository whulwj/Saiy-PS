package ai.saiy.android.command.taxi;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public final class Taxi implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object taxi;

    public Taxi(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.taxi = new Taxi_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.taxi = new Taxi_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.taxi = new Taxi_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Taxi_en) taxi).detectCallable();
            case ENGLISH_US:
                return ((Taxi_en) taxi).detectCallable();
            default:
                return ((Taxi_en) taxi).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
