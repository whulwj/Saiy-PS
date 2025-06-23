package ai.saiy.android.command.horoscope;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public class Horoscope_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Horoscope_en.class.getSimpleName();
    private static String horoscope;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Horoscope_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (horoscope == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static CommandHoroscopeValues sortHoroscope(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long startTime = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final CommandHoroscopeValues commandHoroscopeValues = new CommandHoroscopeValues();
        commandHoroscopeValues.setName("");
        commandHoroscopeValues.setSign(CommandHoroscopeValues.Sign.UNKNOWN);
        ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        if (horoscope == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        final String capricorn = sr.getString(R.string.capricorn);
        final String aquarius = sr.getString(R.string.aquarius);
        final String pisces = sr.getString(R.string.pisces);
        final String aries = sr.getString(R.string.aries);
        final String taurus = sr.getString(R.string.taurus);
        final String gemini = sr.getString(R.string.gemini);
        final String cancer = sr.getString(R.string.cancer);
        final String leo = sr.getString(R.string.leo);
        final String virgo = sr.getString(R.string.virgo);
        final String scorpio = sr.getString(R.string.scorpio);
        final String sagittarius = sr.getString(R.string.sagittarius);
        final String libra = sr.getString(R.string.libra);
        String vdLower;
        for (String voiceDatum : voiceData) {
            vdLower = voiceDatum.toLowerCase(locale).trim();
            if (vdLower.contains(horoscope)) {
                if (vdLower.contains(capricorn)) {
                    commandHoroscopeValues.setName(capricorn);
                    commandHoroscopeValues.setSign(CommandHoroscopeValues.Sign.CAPRICORN);
                    break;
                }
                if (vdLower.contains(aquarius)) {
                    commandHoroscopeValues.setName(aquarius);
                    commandHoroscopeValues.setSign(CommandHoroscopeValues.Sign.AQUARIUS);
                    break;
                }
                if (vdLower.contains(pisces)) {
                    commandHoroscopeValues.setName(pisces);
                    commandHoroscopeValues.setSign(CommandHoroscopeValues.Sign.PISCES);
                    break;
                }
                if (vdLower.contains(aries)) {
                    commandHoroscopeValues.setName(aries);
                    commandHoroscopeValues.setSign(CommandHoroscopeValues.Sign.ARIES);
                    break;
                }
                if (vdLower.contains(taurus)) {
                    commandHoroscopeValues.setName(taurus);
                    commandHoroscopeValues.setSign(CommandHoroscopeValues.Sign.TAURUS);
                    break;
                }
                if (vdLower.contains(gemini)) {
                    commandHoroscopeValues.setName(gemini);
                    commandHoroscopeValues.setSign(CommandHoroscopeValues.Sign.GEMINI);
                    break;
                }
                if (vdLower.contains(cancer)) {
                    commandHoroscopeValues.setName(cancer);
                    commandHoroscopeValues.setSign(CommandHoroscopeValues.Sign.CANCER);
                    break;
                }
                if (vdLower.contains(leo)) {
                    commandHoroscopeValues.setName(leo);
                    commandHoroscopeValues.setSign(CommandHoroscopeValues.Sign.LEO);
                    break;
                }
                if (vdLower.contains(virgo)) {
                    commandHoroscopeValues.setName(virgo);
                    commandHoroscopeValues.setSign(CommandHoroscopeValues.Sign.VIRGO);
                    break;
                }
                if (vdLower.contains(scorpio)) {
                    commandHoroscopeValues.setName(scorpio);
                    commandHoroscopeValues.setSign(CommandHoroscopeValues.Sign.SCORPIO);
                    break;
                }
                if (vdLower.contains(sagittarius)) {
                    commandHoroscopeValues.setName(sagittarius);
                    commandHoroscopeValues.setSign(CommandHoroscopeValues.Sign.SAGITTARIUS);
                    break;
                }
                if (vdLower.contains(libra)) {
                    commandHoroscopeValues.setName(libra);
                    commandHoroscopeValues.setSign(CommandHoroscopeValues.Sign.LIBRA);
                    break;
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, startTime);
        }
        return commandHoroscopeValues;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        horoscope = sr.getString(R.string.horoscope);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long startTime = System.nanoTime();
        ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                if (this.voiceData.get(i).toLowerCase(locale).trim().contains(horoscope)) {
                    toReturn.add(new Pair<>(CC.COMMAND_HOROSCOPE, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "horoscope: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, startTime);
        }
        return toReturn;
    }
}
