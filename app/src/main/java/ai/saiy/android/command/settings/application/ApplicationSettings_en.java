package ai.saiy.android.command.settings.application;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public class ApplicationSettings_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = ApplicationSettings_en.class.getSimpleName();

    private static String application_settings;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public ApplicationSettings_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (application_settings == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static ArrayList<String> detectApplicationNames(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();
        if (application_settings == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        final String applicationSettingsCommand = application_settings + Constants.SEP_SPACE;
        final int size = voiceData.size();
        String vdLower;
        for (int i = 0; i < size; i++) {
            vdLower = voiceData.get(i).toLowerCase(locale).trim();
            if (vdLower.startsWith(applicationSettingsCommand)) {
                String applicationName = vdLower.replaceFirst(applicationSettingsCommand, "").trim();
                if (applicationName.matches("say")) {
                    applicationName = Constants.SAIY;
                }
                toReturn.add(applicationName);
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        application_settings = sr.getString(R.string.application_settings);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final String applicationSettingsCommand = application_settings + Constants.SEP_SPACE;
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            String vdLower;
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.startsWith(applicationSettingsCommand)) {
                    toReturn.add(new Pair<>(CC.COMMAND_APPLICATION_SETTINGS, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "application settings: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
