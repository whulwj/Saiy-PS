package ai.saiy.android.command.remember;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public final class Remember implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object remember;

    public Remember(@NonNull SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Remember(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.remember = new Remember_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.remember = new Remember_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.remember = new Remember_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Remember_en) remember).detectCallable();
            case ENGLISH_US:
                return ((Remember_en) remember).detectCallable();
            default:
                return ((Remember_en) remember).detectCallable();
        }
    }

    public ArrayList<String> fetchTarget(@NonNull Context context, @NonNull ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Remember_en.fetchTarget(context, voiceData, sl);
            case ENGLISH_US:
                return Remember_en.fetchTarget(context, voiceData, sl);
            default:
                return Remember_en.fetchTarget(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
