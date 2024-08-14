package ai.saiy.android.command.location.vehicle.locate;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class LocateVehicle_en {
    private static String find_my;
    private static String wheres_my;
    private static String locate_my;
    private static String vehicle;
    private static String car;
    private static String bike;
    private static String bicycle;
    private static String where;
    private static String park;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = LocateVehicle_en.class.getSimpleName();
    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public LocateVehicle_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (find_my == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        find_my = sr.getString(R.string.find_my);
        wheres_my = sr.getString(R.string.wheres_my);
        locate_my = sr.getString(R.string.locate_my);
        vehicle = sr.getString(R.string.vehicle);
        car = sr.getString(R.string.car);
        bike = sr.getString(R.string.bike);
        bicycle = sr.getString(R.string.bicycle);
        where = sr.getString(R.string.where);
        park = sr.getString(R.string.park);
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
                if (vdLower.startsWith(find_my) || vdLower.startsWith(locate_my) || vdLower.startsWith(wheres_my)) {
                    if (vdLower.contains(car) || vdLower.contains(bike) || vdLower.contains(LocateVehicle_en.bicycle) || vdLower.contains(vehicle)) {
                        toReturn.add(new Pair<>(CC.COMMAND_LOCATE_VEHICLE, confidence[i]));
                    }
                } else if (vdLower.startsWith(where) && vdLower.contains(park) && (vdLower.contains(car) || vdLower.contains(bike) || vdLower.contains(LocateVehicle_en.bicycle) || vdLower.contains(vehicle))) {
                    toReturn.add(new Pair<>(CC.COMMAND_LOCATE_VEHICLE, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "locate vehicle: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
