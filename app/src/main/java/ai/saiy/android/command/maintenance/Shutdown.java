package ai.saiy.android.command.maintenance;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Shutdown implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object shutdown;

    public Shutdown(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.shutdown = new Shutdown_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.shutdown = new Shutdown_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.shutdown = new Shutdown_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Shutdown_en) shutdown).detectCallable();
            case ENGLISH_US:
                return ((Shutdown_en) shutdown).detectCallable();
            default:
                return ((Shutdown_en) shutdown).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
