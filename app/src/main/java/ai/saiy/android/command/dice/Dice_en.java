package ai.saiy.android.command.dice;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class Dice_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Dice_en.class.getSimpleName();

    private static String roll;
    private static String dice;
    private static String dices;
    private static String die;
    private static String word_throw;
    private static String a;
    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Dice_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (roll == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static long sortDice(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();

        if (roll == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
            sr.reset();
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        long result = 0;
        for (String voiceDatum : voiceData) {
            String vdLower = voiceDatum.toLowerCase(locale).trim().replaceAll(roll, "").trim().replaceAll(word_throw, "").trim().replaceAll(dices, "").trim().replaceAll(dice, "").trim().replaceAll(die, "").trim().replaceAll("\\b" + a + "\\b", "").trim();
            if (!vdLower.isEmpty()) {
                try {
                    result = Long.parseLong(vdLower);
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "NumberFormatException: " + vdLower);
                    }
                } catch (Exception e) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "Exception: " + vdLower);
                    }
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return result;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        roll = sr.getString(R.string.roll);
        dice = sr.getString(R.string.dice);
        dices = sr.getString(R.string.dices);
        die = sr.getString(R.string.die);
        word_throw = sr.getString(R.string.word_throw);
        a = sr.getString(R.string.a);
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
                if ((vdLower.startsWith(roll) || vdLower.startsWith(word_throw)) && (vdLower.contains(dice) || vdLower.contains(die))) {
                    toReturn.add(new Pair<>(CC.COMMAND_DICE, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "dice: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
