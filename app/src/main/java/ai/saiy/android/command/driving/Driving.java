package ai.saiy.android.command.driving;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Driving implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object driving;

    public Driving(@NonNull SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Driving(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.driving = new Driving_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.driving = new Driving_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.driving = new Driving_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public CommandDrivingValues fetch(@NonNull Context context, @NonNull ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Driving_en.sortDriving(context, voiceData, sl);
            case ENGLISH_US:
                return Driving_en.sortDriving(context, voiceData, sl);
            default:
                return Driving_en.sortDriving(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Driving_en) driving).detectCallable();
            case ENGLISH_US:
                return ((Driving_en) driving).detectCallable();
            default:
                return ((Driving_en) driving).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
