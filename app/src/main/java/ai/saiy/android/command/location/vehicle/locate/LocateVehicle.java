package ai.saiy.android.command.location.vehicle.locate;

import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;

public class LocateVehicle implements Callable<ArrayList<Pair<CC, Float>>> {
    private final SupportedLanguage sl;
    private final Object locateVehicle;

    public LocateVehicle(ai.saiy.android.localisation.SaiyResources aVar, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        switch (supportedLanguage) {
            case ENGLISH:
                this.locateVehicle = new LocateVehicle_en(aVar, supportedLanguage, voiceData, confidence);
                break;
            case ENGLISH_US:
                this.locateVehicle = new LocateVehicle_en(aVar, supportedLanguage, voiceData, confidence);
                break;
            default:
                this.locateVehicle = new LocateVehicle_en(aVar, SupportedLanguage.ENGLISH, voiceData, confidence);
                break;
        }
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        switch (sl) {
            case ENGLISH:
                return ((LocateVehicle_en) locateVehicle).detectCallable();
            case ENGLISH_US:
                return ((LocateVehicle_en) locateVehicle).detectCallable();
            default:
                return ((LocateVehicle_en) locateVehicle).detectCallable();
        }
    }

    @Override
    public ArrayList<Pair<CC, Float>> call() throws Exception {
        return detectCallable();
    }
}
