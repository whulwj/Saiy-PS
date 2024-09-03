package ai.saiy.android.command.help;

import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Help implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object help;

    public Help(ai.saiy.android.localisation.SaiyResources aVar, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.help = new Help_en(aVar, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.help = new Help_en(aVar, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.help = new Help_en(aVar, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Help_en) help).detectCallable();
            case ENGLISH_US:
                return ((Help_en) help).detectCallable();
            default:
                return ((Help_en) help).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
