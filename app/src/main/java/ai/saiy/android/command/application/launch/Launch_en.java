package ai.saiy.android.command.application.launch;

import android.content.Context;
import android.util.Pair;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public class Launch_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Launch_en.class.getSimpleName();
    private static String launch;
    private static String open;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Launch_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (launch == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static ArrayList<String> detectLaunch(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();
        final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        if (launch == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        final String the = sr.getString(R.string.the);
        final String application = sr.getString(R.string.application);
        final String app = sr.getString(R.string.app);
        final String launchPrefix = launch + XMLResultsHandler.SEP_SPACE;
        final String openPrefix = open + XMLResultsHandler.SEP_SPACE;
        String[] split = null;
        for (String voiceDatum : voiceData) {
            String vdLower = voiceDatum.toLowerCase(locale).trim();
            if (vdLower.startsWith(launchPrefix)) {
                split = vdLower.split(launchPrefix);
            } else if (vdLower.startsWith(openPrefix)) {
                split = vdLower.split(openPrefix);
            }
            if (split != null && split.length > 1) {
                String applicationName = split[1].trim().replaceFirst(the + XMLResultsHandler.SEP_SPACE + application, "").trim().replaceFirst(the + XMLResultsHandler.SEP_SPACE + app, "").trim();
                if (applicationName.startsWith(application)) {
                    applicationName = applicationName.replaceFirst(application, "").trim();
                } else if (applicationName.startsWith(app)) {
                    applicationName = applicationName.replaceFirst(app, "").trim();
                }
                if (applicationName.matches("say")) {
                    applicationName = Constants.SAIY;
                }
                toReturn.add(applicationName);
            }
        }
        if (!toReturn.isEmpty()) {
            LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>(toReturn);
            toReturn.clear();
            toReturn.addAll(linkedHashSet);
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        launch = sr.getString(R.string.launch);
        open = sr.getString(R.string.open);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (UtilsList.notNaked(voiceData) && UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            final int size = voiceData.size();
            String vdLower;
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.startsWith(launch) || vdLower.startsWith(open)) {
                    toReturn.add(new Pair<>(CC.COMMAND_LAUNCH, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "launch: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
