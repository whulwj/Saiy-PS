package ai.saiy.android.command.calculate;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public final class Calculate_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Calculate_en.class.getSimpleName();
    private static String calculate;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Calculate_en(@NonNull ai.saiy.android.localisation.SaiyResources sr, @NonNull SupportedLanguage supportedLanguage, @NonNull ArrayList<String> voiceData, @NonNull float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (calculate == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static ArrayList<String> sortCalculation(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();
        final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        if (calculate == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        final String calculation = calculate + XMLResultsHandler.SEP_SPACE;
        final String divided_by = sr.getString(R.string._DIVIDED_BY_);
        final String divided = sr.getString(R.string._DIVIDED_);
        final String divide_by = sr.getString(R.string._DIVIDE_BY_);
        final String divide = sr.getString(R.string._DIVIDE_);
        final String plus = sr.getString(R.string._PLUS_);
        final String minus = sr.getString(R.string._MINUS_);
        final String times = sr.getString(R.string._TIMES_);
        final String time = sr.getString(R.string._TIME);
        final String add = sr.getString(R.string._ADD_);
        final String multiplied_by = sr.getString(R.string._MULTIPLIED_BY_);
        final String multiplied = sr.getString(R.string._MULTIPLIED_);
        final String subtracted = sr.getString(R.string._SUBTRACTED_);
        final String subtract = sr.getString(R.string._SUBTRACT_);
        final String to_the_power_of = sr.getString(R.string._TO_THE_POWER_OF_);
        final String to_the_power = sr.getString(R.string._TO_THE_POWER_);
        final String squared = sr.getString(R.string._SQUARED);
        final String cubed = sr.getString(R.string._CUBED);
        final String square_root = sr.getString(R.string.SQUARE_ROOT);
        final String the = sr.getString(R.string.the);
        final String of = sr.getString(R.string.of);
        final String equals = sr.getString(R.string.EQUALS);
        final String equal = sr.getString(R.string.EQUAL);
        final String percent = sr.getString(R.string.PERCENT);
        sr.reset();
        for (String voiceDatum : voiceData) {
            String vdLower = voiceDatum.toLowerCase(locale).trim();
            if (vdLower.startsWith(calculation)) {
                toReturn.add(vdLower.replaceFirst(calculation, "").replaceAll(divided_by, " / ").replaceAll(divided, " / ").replaceAll(divide_by, " / ").replaceAll(divide, " / ").replaceAll(plus, " + ").replaceAll(minus, " - ").replaceAll(XMLResultsHandler.SEP_HYPHEN, " - ").replaceAll(times, " \\* ").replaceAll(time, " * ").replaceAll(add, " + ").replaceAll(multiplied_by, " * ").replaceAll(multiplied, " * ").replaceAll(subtracted, " - ").replaceAll(subtract, "- ").replaceAll(percent, "% ").replaceAll(to_the_power_of, " ^ ").replaceAll(to_the_power, " ^ ").replaceAll(squared, " ^ 2 ").replaceAll(cubed, " ^ 3 ").replaceAll(square_root, MathEval.SQUARE_ROOT).replaceAll(calculate, XMLResultsHandler.SEP_SPACE).replaceAll(the, "").replaceAll(of, "").replaceAll(equals, XMLResultsHandler.SEP_SPACE).replaceAll(equal, XMLResultsHandler.SEP_SPACE).trim().replaceAll(" +", XMLResultsHandler.SEP_SPACE));
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }

    private static void initStrings(@NonNull ai.saiy.android.localisation.SaiyResources sr) {
        calculate = sr.getString(R.string.calculate);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.startsWith(calculate)) {
                    toReturn.add(new Pair<>(CC.COMMAND_CALCULATE, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "calculate: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
