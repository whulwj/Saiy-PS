package ai.saiy.android.command.weather;

import android.content.Context;
import android.util.Pair;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public class Weather_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Weather_en.class.getSimpleName();
    private static String weather;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Weather_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (weather == null) {
            if (DEBUG) {
               MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
           MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static ArrayList<String> sortWeather(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();
        ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        if (weather == null) {
            if (DEBUG) {
               MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
           MyLog.i(CLS_NAME, "strings initialised");
        }
        final String in = sr.getString(R.string.in);
        sr.reset();
        String vdLower;
        for (String vdDatum : voiceData) {
            vdLower = vdDatum.toLowerCase(locale).trim();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "examining: " + vdLower);
            }
            if (vdLower.contains(weather + XMLResultsHandler.SEP_SPACE)) {
                String[] separated = vdLower.split(weather + XMLResultsHandler.SEP_SPACE);
                if (separated.length > 1) {
                    String trimmed = separated[1].trim();
                    if (trimmed.matches(".*\\b" + in + "\\b.*")) {
                        separated = trimmed.split("\\b" + in + "\\b", 2);
                        if (separated.length > 1) {
                            trimmed = separated[1].trim();
                            if (UtilsString.notNaked(trimmed)) {
                                toReturn.add(trimmed);
                            }
                        }
                    }
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        weather = sr.getString(R.string.weather);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                if (voiceData.get(i).toLowerCase(locale).trim().contains(weather)) {
                    toReturn.add(new Pair<>(CC.COMMAND_WEATHER, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "weather: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
