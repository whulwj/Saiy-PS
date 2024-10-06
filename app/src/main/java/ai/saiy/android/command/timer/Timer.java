package ai.saiy.android.command.timer;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public final class Timer implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object timer;

    public Timer(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Timer(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.timer = new Timer_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.timer = new Timer_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.timer = new Timer_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Timer_en) timer).detectCallable();
            case ENGLISH_US:
                return ((Timer_en) timer).detectCallable();
            default:
                return ((Timer_en) timer).detectCallable();
        }
    }

    public ArrayList<String> sortTimer(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Timer_en.sortTimer(context, voiceData, sl);
            case ENGLISH_US:
                return Timer_en.sortTimer(context, voiceData, sl);
            default:
                return Timer_en.sortTimer(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
