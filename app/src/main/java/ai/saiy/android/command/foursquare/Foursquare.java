package ai.saiy.android.command.foursquare;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Foursquare implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private Object foursquare;

    public Foursquare(SupportedLanguage supportedLanguage) {
        this.sl = supportedLanguage;
    }

    public Foursquare(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.foursquare = new Foursquare_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.foursquare = new Foursquare_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.foursquare = new Foursquare_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Foursquare_en) foursquare).detectCallable();
            case ENGLISH_US:
                return ((Foursquare_en) foursquare).detectCallable();
            default:
                return ((Foursquare_en) foursquare).detectCallable();
        }
    }

    public ArrayList<String> sort(Context context, ArrayList<String> voiceData) {
        switch (sl) {
            case ENGLISH:
                return Foursquare_en.sortFoursquare(context, voiceData, sl);
            case ENGLISH_US:
                return Foursquare_en.sortFoursquare(context, voiceData, sl);
            default:
                return Foursquare_en.sortFoursquare(context, voiceData, SupportedLanguage.ENGLISH);
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
