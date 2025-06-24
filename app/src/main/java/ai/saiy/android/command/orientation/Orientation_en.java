package ai.saiy.android.command.orientation;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;

public class Orientation_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Orientation_en.class.getSimpleName();
    private static String landscape;
    private static String portrait;
    private static String port_rate;
    private static String reverse;
    private static String rotate;
    private static String rotation;
    private static String lock;
    private static String locked;
    private static String unlock;
    private static String unlocked;
    private static String orientation;
    private static String screen;
    private static String display;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Orientation_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (landscape == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static @NonNull CommandOrientationValues detectOrientation(android.content.Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final CommandOrientationValues commandOrientationValues = new CommandOrientationValues();
        commandOrientationValues.setDescription("");
        commandOrientationValues.setType(CommandOrientationValues.Type.UNKNOWN);
        if (landscape == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        String vdLower;
        for (String voiceDatum : voiceData) {
            vdLower = voiceDatum.toLowerCase(locale).trim();
            if ((vdLower.contains(rotate) || vdLower.contains(rotation) || vdLower.contains(screen) || vdLower.contains(display) || vdLower.contains(orientation))
                    && (vdLower.contains(landscape) || vdLower.contains(port_rate) || vdLower.contains(portrait))) {
                if (vdLower.contains(landscape)) {
                    if (vdLower.contains(reverse)) {
                        commandOrientationValues.setDescription(reverse + Constants.SEP_SPACE + landscape);
                        commandOrientationValues.setType(CommandOrientationValues.Type.REVERSE_LANDSCAPE);
                    } else {
                        commandOrientationValues.setDescription(landscape);
                        commandOrientationValues.setType(CommandOrientationValues.Type.LANDSCAPE);
                    }
                    break;
                }
                if (vdLower.contains(portrait) || vdLower.contains(port_rate)) {
                    if (vdLower.contains(reverse)) {
                        commandOrientationValues.setDescription(reverse + Constants.SEP_SPACE + portrait);
                        commandOrientationValues.setType(CommandOrientationValues.Type.REVERSE_PORTRAIT);
                    } else {
                        commandOrientationValues.setDescription(portrait);
                        commandOrientationValues.setType(CommandOrientationValues.Type.PORTRAIT);
                    }
                    break;
                }
            } else if ((vdLower.startsWith(lock) || vdLower.startsWith(unlock)) && (vdLower.contains(orientation) || vdLower.contains(rotation))) {
                if (vdLower.contains(unlock)) {
                    commandOrientationValues.setDescription(unlocked);
                    commandOrientationValues.setType(CommandOrientationValues.Type.UNLOCK);
                    break;
                }
                if (vdLower.contains(lock)) {
                    commandOrientationValues.setDescription(locked);
                    commandOrientationValues.setType(CommandOrientationValues.Type.LOCK);
                    break;
                }
            } else if (vdLower.matches(port_rate) || vdLower.matches(portrait)) {
                commandOrientationValues.setDescription(portrait);
                commandOrientationValues.setType(CommandOrientationValues.Type.PORTRAIT);
                break;
            } else if (vdLower.matches(reverse + Constants.SEP_SPACE + port_rate) || vdLower.matches(reverse + Constants.SEP_SPACE + portrait)) {
                commandOrientationValues.setDescription(reverse + Constants.SEP_SPACE + portrait);
                commandOrientationValues.setType(CommandOrientationValues.Type.REVERSE_PORTRAIT);
                break;
            } else if (vdLower.matches(landscape)) {
                commandOrientationValues.setDescription(landscape);
                commandOrientationValues.setType(CommandOrientationValues.Type.LANDSCAPE);
                break;
            } else if (vdLower.matches(reverse + Constants.SEP_SPACE + landscape)) {
                commandOrientationValues.setDescription(reverse + Constants.SEP_SPACE + landscape);
                commandOrientationValues.setType(CommandOrientationValues.Type.REVERSE_LANDSCAPE);
                break;
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return commandOrientationValues;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        landscape = sr.getString(R.string.landscape);
        portrait = sr.getString(R.string.portrait);
        port_rate = sr.getString(R.string.port_rate);
        reverse = sr.getString(R.string.reverse);
        rotate = sr.getString(R.string.rotate);
        rotation = sr.getString(R.string.rotation);
        lock = sr.getString(R.string.lock);
        unlock = sr.getString(R.string.unlock);
        locked = sr.getString(R.string.locked);
        unlocked = sr.getString(R.string.unlocked);
        orientation = sr.getString(R.string.orientation);
        screen = sr.getString(R.string.screen);
        display = sr.getString(R.string.display);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            for (int i = 0; i < size; i++) {
                String vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.matches(port_rate) || vdLower.matches(portrait) || vdLower.matches(landscape) || vdLower.matches(Orientation_en.reverse + Constants.SEP_SPACE + port_rate) || vdLower.matches(Orientation_en.reverse + portrait) || vdLower.matches(Orientation_en.reverse + Constants.SEP_SPACE + landscape)) {
                    toReturn.add(new Pair<>(CC.COMMAND_ORIENTATION, confidence[i]));
                } else if ((vdLower.contains(rotate) || vdLower.contains(rotation) || vdLower.contains(screen) || vdLower.contains(display) || vdLower.contains(orientation) || vdLower.contains(Orientation_en.reverse)) && (vdLower.contains(landscape) || vdLower.contains(port_rate) || vdLower.contains(portrait))) {
                    toReturn.add(new Pair<>(CC.COMMAND_ORIENTATION, confidence[i]));
                } else if ((vdLower.startsWith(lock) || vdLower.startsWith(unlock)) && (vdLower.contains(rotation) || vdLower.contains(orientation))) {
                    toReturn.add(new Pair<>(CC.COMMAND_ORIENTATION, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "orientation: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
