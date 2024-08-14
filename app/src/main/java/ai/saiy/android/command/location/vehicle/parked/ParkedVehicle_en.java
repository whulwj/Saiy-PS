package ai.saiy.android.command.location.vehicle.parked;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class ParkedVehicle_en {
    private static String parked;
    private static String bike;
    private static String bicycle;
    private static String car;
    private static String vehicle;
    private static String where;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ParkedVehicle_en.class.getSimpleName();

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public ParkedVehicle_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (parked == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        parked = sr.getString(R.string.parked);
        car = sr.getString(R.string.car);
        vehicle = sr.getString(R.string.vehicle);
        bicycle = sr.getString(R.string.bicycle);
        bike = sr.getString(R.string.bike);
        where = sr.getString(R.string.where);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            String vdLower;
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (!vdLower.contains(where) && vdLower.contains(parked) && (vdLower.contains(car) || vdLower.contains(bicycle) || vdLower.contains(bike) || vdLower.contains(vehicle))) {
                    toReturn.add(new Pair<>(CC.COMMAND_PARKED_VEHICLE, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "parked vehicle: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
