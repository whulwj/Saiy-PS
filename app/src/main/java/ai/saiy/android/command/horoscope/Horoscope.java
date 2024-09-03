package ai.saiy.android.command.horoscope;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Horoscope implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object horoscope;

    public Horoscope(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Horoscope(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.horoscope = new Horoscope_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.horoscope = new Horoscope_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.horoscope = new Horoscope_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public CommandHoroscopeValues fetch(Context context, ArrayList<String> voiceData) {
        switch (this.sl) {
            case ENGLISH:
                return Horoscope_en.sortHoroscope(context, voiceData, sl);
            case ENGLISH_US:
                return Horoscope_en.sortHoroscope(context, voiceData, sl);
            default:
                return Horoscope_en.sortHoroscope(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (this.sl) {
            case ENGLISH:
                return ((Horoscope_en) horoscope).detectCallable();
            case ENGLISH_US:
                return ((Horoscope_en) horoscope).detectCallable();
            default:
                return ((Horoscope_en) horoscope).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
