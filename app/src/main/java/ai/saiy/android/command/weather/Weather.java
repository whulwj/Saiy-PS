package ai.saiy.android.command.weather;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Weather implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object weather;

    public Weather(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Weather(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.weather = new Weather_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.weather = new Weather_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.weather = new Weather_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Weather_en) weather).detectCallable();
            case ENGLISH_US:
                return ((Weather_en) weather).detectCallable();
            default:
                return ((Weather_en) weather).detectCallable();
        }
    }

    public ArrayList<String> sort(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Weather_en.sortWeather(context, voiceData, sl);
            case ENGLISH_US:
                return Weather_en.sortWeather(context, voiceData, sl);
            default:
                return Weather_en.sortWeather(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
