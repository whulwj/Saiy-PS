package ai.saiy.android.command.location.address;

import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class Location implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object location;

    public Location(ai.saiy.android.localisation.SaiyResources aVar, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.location = new Location_en(aVar, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.location = new Location_en(aVar, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.location = new Location_en(aVar, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((Location_en) location).detectCallable();
            case ENGLISH_US:
                return ((Location_en) location).detectCallable();
            default:
                return ((Location_en) location).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
