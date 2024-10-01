package ai.saiy.android.command.taxi;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public final class Taxi_en {
    private static String taxi;
    private static String cab;
    private static Pattern pNeed;
    private static Pattern pGet;
    private static Pattern pCall;
    private static Pattern pOrder;
    private static Pattern pTaxi;
    private static Pattern pCab;
    private static Pattern pCar;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Taxi_en.class.getSimpleName();

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Taxi_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (taxi == null || pTaxi == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        taxi = sr.getString(R.string.taxi);
        cab = sr.getString(R.string.cab);
        pTaxi = Pattern.compile("\\b" + taxi + "\\b");
        pCab = Pattern.compile("\\b" + cab + "\\b");
        pCar = Pattern.compile("\\b" + sr.getString(R.string.car) + "\\b");
        pNeed = Pattern.compile("\\b" + sr.getString(R.string.need) + "\\b");
        pGet = Pattern.compile("\\b" + sr.getString(R.string.get) + "\\b");
        pCall = Pattern.compile("\\b" + sr.getString(R.string.call) + "\\b");
        pOrder = Pattern.compile("\\b" + sr.getString(R.string.order) + "\\b");
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            Locale locale = sl.getLocale();
            int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.matches(taxi) || vdLower.matches(cab)) {
                    toReturn.add(new Pair<>(CC.COMMAND_TAXI, confidence[i]));
                } else if ((pOrder.matcher(vdLower).find() || pCall.matcher(vdLower).find() || pGet.matcher(vdLower).find() || pNeed.matcher(vdLower).find()) && (Taxi_en.pTaxi.matcher(vdLower).find() || pCab.matcher(vdLower).find() || pCar.matcher(vdLower).find())) {
                    toReturn.add(new Pair<>(CC.COMMAND_TAXI, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "taxi: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
