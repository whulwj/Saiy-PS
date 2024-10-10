package ai.saiy.android.command.uninstall;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public final class Uninstall implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object uninstall;

    public Uninstall(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.uninstall = new Uninstall_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.uninstall = new Uninstall_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.uninstall = new Uninstall_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Uninstall_en) uninstall).detectCallable();
            case ENGLISH_US:
                return ((Uninstall_en) uninstall).detectCallable();
            default:
                return ((Uninstall_en) uninstall).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
