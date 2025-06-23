package ai.saiy.android.command.application.kill;

import android.content.Context;
import android.util.Pair;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public class Kill_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Kill_en.class.getSimpleName();
    private static String kill;
    private static String killPrefix;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Kill_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (kill == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static ArrayList<String> detectKill(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();
        final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
        if (kill == null) {
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
        for (String voiceDatum : voiceData) {
            String vdLower = voiceDatum.toLowerCase(locale).trim();
            if (vdLower.startsWith(killPrefix)) {
                String[] split = vdLower.split(killPrefix);
                if (split.length > 1) {
                    String applicationName = split[1].trim().replaceFirst(the + XMLResultsHandler.SEP_SPACE + application, "").trim().replaceFirst(the + XMLResultsHandler.SEP_SPACE + app, "").trim();
                    if (applicationName.startsWith(application)) {
                        applicationName = applicationName.replaceFirst(application, "").trim();
                    } else if (applicationName.startsWith(app)) {
                        applicationName = applicationName.replaceFirst(app, "").trim();
                    }
                    toReturn.add(applicationName);
                }
            }
        }
        if (!toReturn.isEmpty()) {
            final LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>(toReturn);
            toReturn.clear();
            toReturn.addAll(linkedHashSet);
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        kill = sr.getString(R.string.kill);
        killPrefix = kill + XMLResultsHandler.SEP_SPACE;
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
                if (vdLower.startsWith(killPrefix)) {
                    toReturn.add(new Pair<>(CC.COMMAND_KILL, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "kill: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
