package ai.saiy.android.command.toast;

import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public final class Toast implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object toast;

    public Toast(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.toast = new Toast_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.toast = new Toast_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.toast = new Toast_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Toast_en) toast).detectCallable();
            case ENGLISH_US:
                return ((Toast_en) toast).detectCallable();
            default:
                return ((Toast_en) toast).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
