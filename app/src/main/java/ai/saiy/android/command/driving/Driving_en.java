package ai.saiy.android.command.driving;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.OnOff;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public class Driving_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Driving_en.class.getSimpleName();

    private static String driving;
    private static String profile;
    private static String i_m_driving;
    private static String i_am_driving;
    private static String i_ve_started_driving;
    private static OnOff onOff;
    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Driving_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (onOff == null || driving == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static CommandDrivingValues sortDriving(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long startTime = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final CommandDrivingValues commandDrivingValues = new CommandDrivingValues();
        commandDrivingValues.setAction(CommandDrivingValues.Action.UNKNOWN);
        if (driving == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
            sr.reset();
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        final OnOff.Result result = onOff.resolve(voiceData, supportedLanguage);
        String vdLower;
        for (String vd : voiceData) {
            vdLower = vd.toLowerCase(locale).trim();
            if (vdLower.contains(driving) && vdLower.contains(profile) && result != OnOff.Result.UNRESOLVED) {
                switch (result) {
                    case ON:
                        commandDrivingValues.setAction(CommandDrivingValues.Action.ENABLE);
                        break;
                    case OFF:
                        commandDrivingValues.setAction(CommandDrivingValues.Action.DISABLE);
                        break;
                    case TOGGLE:
                        commandDrivingValues.setAction(CommandDrivingValues.Action.TOGGLE);
                        break;
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, startTime);
        }
        return commandDrivingValues;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        driving = sr.getString(R.string.driving);
        profile = sr.getString(R.string.profile);
        i_m_driving = sr.getString(R.string.i_m_driving);
        i_am_driving = sr.getString(R.string.i_am_driving);
        i_ve_started_driving = sr.getString(R.string.i_ve_started_driving);
        onOff = new OnOff(sr);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long startTime = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(this.voiceData) && UtilsList.notNaked(this.confidence) && this.voiceData.size() == this.confidence.length) {
            final OnOff.Result result = onOff.resolve(this.voiceData, this.sl);
            final Locale locale = this.sl.getLocale();
            String vdLower;
            final int size = this.voiceData.size();
            for (int i = 0; i < size; i++) {
                vdLower = this.voiceData.get(i).toLowerCase(locale).trim();
                if (!vdLower.contains(driving) || !vdLower.contains(profile) || result == OnOff.Result.UNRESOLVED) {
                    if (vdLower.startsWith(i_m_driving) || vdLower.startsWith(i_am_driving) || vdLower.startsWith(i_ve_started_driving)) {
                        toReturn.add(new Pair<>(CC.COMMAND_DRIVING, 1.0f));
                        break;
                    }
                } else {
                    toReturn.add(new Pair<>(CC.COMMAND_DRIVING, this.confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "driving: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, startTime);
        }
        return toReturn;
    }
}
