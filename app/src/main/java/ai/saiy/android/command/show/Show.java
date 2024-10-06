package ai.saiy.android.command.show;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Show implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object show;

    public Show(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.show = new Show_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.show = new Show_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.show = new Show_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Show_en) show).detectCallable();
            case ENGLISH_US:
                return ((Show_en) show).detectCallable();
            default:
                return ((Show_en) show).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
