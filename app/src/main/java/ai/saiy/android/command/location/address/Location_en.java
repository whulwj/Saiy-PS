package ai.saiy.android.command.location.address;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class Location_en {
    private static String where_am_i;
    private static String whats_my_current_location;
    private static String whats_my_location;
    private static String current_location;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Location_en.class.getSimpleName();

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Location_en(ai.saiy.android.localisation.SaiyResources aVar, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (where_am_i == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(aVar);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        where_am_i = sr.getString(R.string.where_am_i);
        whats_my_current_location = sr.getString(R.string.whats_my_current_location);
        whats_my_location = sr.getString(R.string.whats_my_location);
        current_location = sr.getString(R.string.current_location);
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
                if (vdLower.startsWith(where_am_i) || vdLower.startsWith(whats_my_current_location) || vdLower.startsWith(whats_my_location) || vdLower.startsWith(current_location)) {
                    toReturn.add(new Pair<>(CC.COMMAND_LOCATION, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "location: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
