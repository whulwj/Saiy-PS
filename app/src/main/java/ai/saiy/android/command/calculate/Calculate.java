package ai.saiy.android.command.calculate;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public final class Calculate implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object calculate;

    public Calculate(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Calculate(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.calculate = new Calculate_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.calculate = new Calculate_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.calculate = new Calculate_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Calculate_en) calculate).detectCallable();
            case ENGLISH_US:
                return ((Calculate_en) calculate).detectCallable();
            default:
                return ((Calculate_en) calculate).detectCallable();
        }
    }

    public ArrayList<String> sortCalculation(@NonNull Context context, @NonNull ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Calculate_en.sortCalculation(context, voiceData, sl);
            case ENGLISH_US:
                return Calculate_en.sortCalculation(context, voiceData, sl);
            default:
                return Calculate_en.sortCalculation(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
