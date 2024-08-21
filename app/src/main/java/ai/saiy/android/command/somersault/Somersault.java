package ai.saiy.android.command.somersault;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.command.orientation.CommandOrientationValues;
import ai.saiy.android.localisation.SupportedLanguage;

public class Somersault implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object somersault;

    public Somersault(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Somersault(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.somersault = new Somersault_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.somersault = new Somersault_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.somersault = new Somersault_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public @NonNull CommandOrientationValues detectSomersault(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Somersault_en.detectSomersault(context, voiceData, sl);
            case ENGLISH_US:
                return Somersault_en.detectSomersault(context, voiceData, sl);
            default:
                return Somersault_en.detectSomersault(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Somersault_en) somersault).detectCallable();
            case ENGLISH_US:
                return ((Somersault_en) somersault).detectCallable();
            default:
                return ((Somersault_en) somersault).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
