package ai.saiy.android.command.location.vehicle.parked;

import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class ParkedVehicle implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object parkedVehicle;

    public ParkedVehicle(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.parkedVehicle = new ParkedVehicle_en(sr, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.parkedVehicle = new ParkedVehicle_en(sr, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.parkedVehicle = new ParkedVehicle_en(sr, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((ParkedVehicle_en) parkedVehicle).detectCallable();
            case ENGLISH_US:
                return ((ParkedVehicle_en) parkedVehicle).detectCallable();
            default:
                return ((ParkedVehicle_en) parkedVehicle).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() {
        return detectCallable();
    }
}
