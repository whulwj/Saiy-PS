package ai.saiy.android.command.foursquare;

import android.content.Context;
import android.util.Pair;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class Foursquare_en {
    private static final boolean DEBUG = ai.saiy.android.utils.MyLog.DEBUG;
    private static final String CLS_NAME = Foursquare_en.class.getSimpleName();
    private static String foursquare;
    private static String four_square;
    private static String check_in_to;
    private static String check_into;
    private static String checkin_to;
    private static String check_me_in_to;
    private static String check_me_into;
    private static String check_in;
    private static String at;
    private static String on;
    private static String the;
    private static String location;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Foursquare_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (foursquare == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static ArrayList<String> sortFoursquare(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();
        if (foursquare == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            ai.saiy.android.localisation.SaiyResources aVar = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(aVar);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        String vdLower;
        for (String voiceDatum : voiceData) {
            vdLower = voiceDatum.toLowerCase(locale).trim();
            if (vdLower.startsWith(check_in) || vdLower.startsWith(check_into) || vdLower.startsWith(checkin_to) || vdLower.startsWith(check_me_in_to) || vdLower.startsWith(check_me_into)) {
                String trimmed = vdLower.replaceFirst(check_in_to, "").trim().replaceFirst(check_in, "").trim().replaceFirst(check_into, "").trim().replaceFirst(checkin_to, "").trim().replaceFirst(check_me_in_to, "").trim().replaceFirst(check_me_into, "").trim().replaceFirst(on + XMLResultsHandler.SEP_SPACE + foursquare, "").trim().replaceFirst(on + XMLResultsHandler.SEP_SPACE + four_square, "").trim().replaceAll(foursquare, "").trim().replaceAll(four_square, "").trim();
                if (trimmed.startsWith(at)) {
                    trimmed = trimmed.replaceFirst(at, "").trim();
                }
                trimmed = trimmed.replaceFirst(the + XMLResultsHandler.SEP_SPACE + location, "").trim();
                if (trimmed.startsWith(location)) {
                    trimmed = trimmed.replaceFirst(location, "").trim();
                }
                toReturn.add(trimmed);
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        foursquare = sr.getString(R.string.foursquare);
        four_square = sr.getString(R.string.four_square);
        check_in_to = sr.getString(R.string.check_in_to);
        check_into = sr.getString(R.string.check_into);
        checkin_to = sr.getString(R.string.checkin_to);
        check_me_in_to = sr.getString(R.string.check_me_in_to);
        check_me_into = sr.getString(R.string.check_me_into);
        check_in = sr.getString(R.string.check_in);
        at = sr.getString(R.string.at);
        on = sr.getString(R.string.on);
        the = sr.getString(R.string.the);
        location = sr.getString(R.string.location);
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
                if (vdLower.startsWith(check_in) || vdLower.startsWith(check_into) || vdLower.startsWith(checkin_to) || vdLower.startsWith(check_me_in_to) || vdLower.startsWith(check_me_into)) {
                    toReturn.add(new Pair<>(CC.COMMAND_FOURSQUARE, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "foursquare: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
