package ai.saiy.android.command.alexa;

import android.content.Context;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.recognition.provider.android.RecognitionNative;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsBundle;
import ai.saiy.android.utils.UtilsList;

public class Alexa_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Alexa_en.class.getSimpleName();

    private static String alexa;
    private static String ask_alexa;
    private static String tell_alexa;
    private static String to;
    private static String to_tell_me;
    private static String tell_me;
    private SupportedLanguage sl;
    private ArrayList<String> voiceData;
    private float[] confidence;

    public Alexa_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (ask_alexa == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public Alexa_en(ai.saiy.android.localisation.SaiyResources sr) {
        alexa = sr.getString(R.string.alexa);
    }

    public static ArrayList<String> sortAlexa(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final ArrayList<String> toReturn = new ArrayList<>();
        if (ask_alexa == null) {
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
            if (vdLower.contains(ask_alexa) || vdLower.contains(tell_alexa) || vdLower.startsWith(alexa)) {
                if (vdLower.contains(ask_alexa)) {
                    vdLower = vdLower.replaceFirst(ask_alexa, "").trim();
                } else if (vdLower.contains(tell_alexa)) {
                    vdLower = vdLower.replaceFirst(tell_alexa, "").trim();
                } else if (vdLower.startsWith(alexa)) {
                    vdLower = vdLower.replaceFirst(alexa, "").trim();
                }
                if (vdLower.startsWith(to_tell_me)) {
                    vdLower = vdLower.replaceFirst(to_tell_me, "").trim();
                }
                if (vdLower.startsWith(tell_me)) {
                    vdLower = vdLower.replaceFirst(tell_me, "").trim();
                }
                if (vdLower.startsWith(to)) {
                    vdLower = vdLower.replaceFirst(to, "").trim();
                }
                toReturn.add(vdLower);
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        alexa = sr.getString(R.string.alexa);
        ask_alexa = sr.getString(R.string.ask_alexa);
        tell_alexa = sr.getString(R.string.tell_alexa);
        to = sr.getString(R.string.to);
        to_tell_me = sr.getString(R.string.to_tell_me);
        tell_me = sr.getString(R.string.tell_me);
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
                if (vdLower.contains(ask_alexa) || vdLower.contains(tell_alexa) || vdLower.startsWith(alexa)) {
                    toReturn.add(new Pair<>(CC.COMMAND_ALEXA, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "alexa: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }

    /**
     * Iterate through the voice data array to see if the user has requested a possible alexa command.
     * <p/>
     * Note - As the speech array will never contain more than ten entries, perhaps implementing a
     * matcher, would probably be overkill.
     *
     * @param results {@link Bundle} containing the voice data
     * @param loc     the {@link SupportedLanguage} {@link Locale}
     * @return true if a command variant is detected
     */
    public boolean detectPartial(Locale loc, android.os.Bundle results) {
        final long then = System.nanoTime();
        boolean hasAlexa = false;

        if (UtilsBundle.notNaked(results)) {
            if (!UtilsBundle.isSuspicious(results)) {
                final ArrayList<String> partialData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                /* handles empty string bug */
                if (UtilsList.notNaked(partialData)) {
                    partialData.removeAll(Collections.singleton(""));
                    if (!partialData.isEmpty()) {
                        String vdLower;
                        int size = partialData.size();
                        for (int i = 0; i < size; i++) {
                            vdLower = partialData.get(i).toLowerCase(loc).trim();
                            if (vdLower.matches(".*\\b" + alexa + "\\b.*")) {

                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "partial vd: " + vdLower);
                                }
                                hasAlexa = true;
                                break;
                            }
                        }
                    }
                }

                if (!hasAlexa) {
                    final ArrayList<String> unstableData = results.getStringArrayList(RecognitionNative.UNSTABLE_RESULTS);

                    /* handles empty string bug */
                    if (UtilsList.notNaked(unstableData)) {
                        unstableData.removeAll(Collections.singleton(""));
                        if (!unstableData.isEmpty()) {
                            String vdLower;
                            int size = unstableData.size();
                            for (int i = 0; i < size; i++) {
                                vdLower = unstableData.get(i).toLowerCase(loc).trim();
                                if (vdLower.matches(".*\\b" + alexa + "\\b.*")) {
                                    if (DEBUG) {
                                        MyLog.i(CLS_NAME, "unstable vd: " + vdLower);
                                    }
                                    hasAlexa = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "Alexa: bundle has been tampered with");
                }
            }
        }

        if (DEBUG) {
            MyLog.i(CLS_NAME, "alexa: returning ~ " + hasAlexa);
            MyLog.getElapsed(CLS_NAME, then);
        }
        return hasAlexa;
    }
}
