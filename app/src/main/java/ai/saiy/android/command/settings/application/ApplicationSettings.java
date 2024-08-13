package ai.saiy.android.command.settings.application;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class ApplicationSettings implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object applicationSettings;

    public ApplicationSettings(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public ApplicationSettings(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.applicationSettings = new ApplicationSettings_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.applicationSettings = new ApplicationSettings_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.applicationSettings = new ApplicationSettings_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((ApplicationSettings_en) applicationSettings).detectCallable();
            case ENGLISH_US:
                return ((ApplicationSettings_en) applicationSettings).detectCallable();
            default:
                return ((ApplicationSettings_en) applicationSettings).detectCallable();
        }
    }

    public ArrayList<String> detectApplicationNames(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return ApplicationSettings_en.detectApplicationNames(context, voiceData, sl);
            case ENGLISH_US:
                return ApplicationSettings_en.detectApplicationNames(context, voiceData, sl);
            default:
                return ApplicationSettings_en.detectApplicationNames(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
