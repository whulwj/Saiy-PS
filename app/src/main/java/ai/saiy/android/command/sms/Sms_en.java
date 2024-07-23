package ai.saiy.android.command.sms;

import android.content.Context;
import android.util.Pair;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class Sms_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Sms_en.class.getSimpleName();
    private static String text;
    private static String sms;
    private static String message;
    private static String read;
    private static String from;
    private static String my;

    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Sms_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (text == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static ArrayList<String> sortSms(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();
        if (read == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
            sr.reset();
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        String vdLower;
        for (String s : voiceData) {
            vdLower = s.toLowerCase(locale).trim();
            if ((vdLower.startsWith(read) && (vdLower.contains(text) || vdLower.contains(message))) || vdLower.contains(sms)) {
                if (vdLower.contains(from)) {
                    String[] separated = vdLower.split(from + XMLResultsHandler.SEP_SPACE);
                    if (separated.length > 1) {
                        String sender = separated[1].trim();
                        if (sender.startsWith(my + XMLResultsHandler.SEP_SPACE)) {
                            toReturn.add(sender.split(my + XMLResultsHandler.SEP_SPACE)[1].trim());
                        } else {
                            toReturn.add(sender);
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
        text = sr.getString(R.string.text);
        read = sr.getString(R.string.read);
        from = sr.getString(R.string.from);
        sms = sr.getString(R.string.sms);
        message = sr.getString(R.string.message);
        my = sr.getString(R.string.my);
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
                if ((vdLower.startsWith(read) && (vdLower.contains(text) || vdLower.contains(message))) || vdLower.contains(sms)) {
                    toReturn.add(new Pair<>(CC.COMMAND_SMS, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "sms: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
