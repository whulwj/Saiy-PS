package ai.saiy.android.command.settings.system;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.command.settings.SettingsIntent;
import ai.saiy.android.localisation.SupportedLanguage;

public class Settings implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object settings;

    public Settings(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Settings(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.settings = new Settings_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.settings = new Settings_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.settings = new Settings_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public @NonNull SettingsIntent detectSettingsIntent(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Settings_en.detectSettingsIntent(context, voiceData, sl);
            case ENGLISH_US:
                return Settings_en.detectSettingsIntent(context, voiceData, sl);
            default:
                return Settings_en.detectSettingsIntent(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Settings_en) settings).detectCallable();
            case ENGLISH_US:
                return ((Settings_en) settings).detectCallable();
            default:
                return ((Settings_en) settings).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
