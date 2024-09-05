package ai.saiy.android.command.hardware;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.OnOff;

public class Hardware implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object hardware;

    public Hardware(@NonNull SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Hardware(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.hardware = new Hardware_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.hardware = new Hardware_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.hardware = new Hardware_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public Pair<HardwareType, OnOff.Result> sortHardware(@NonNull Context context, @NonNull ArrayList<String> voiceData) {
        switch (this.sl) {
            case ENGLISH:
                return Hardware_en.sortHardware(context, voiceData, sl);
            case ENGLISH_US:
                return Hardware_en.sortHardware(context, voiceData, sl);
            default:
                return Hardware_en.sortHardware(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Hardware_en) hardware).detectCallable();
            case ENGLISH_US:
                return ((Hardware_en) hardware).detectCallable();
            default:
                return ((Hardware_en) hardware).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
