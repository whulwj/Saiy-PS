package ai.saiy.android.command.facebook;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class Facebook_en {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = Facebook_en.class.getSimpleName();

    private static String facebook;
    private static String status;
    private static String post;
    private static String saying;
    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Facebook_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (facebook == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static CommandFacebookValues sortFacebook(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final CommandFacebookValues commandFacebookValues = new CommandFacebookValues();
        commandFacebookValues.setResolved(false);
        commandFacebookValues.setText("");
        if (facebook == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
        String vdLower;
        for (String voiceDatum : voiceData) {
            vdLower = voiceDatum.toLowerCase(locale).trim();
            if (vdLower.matches(facebook)) {
                break;
            }
            if (vdLower.contains(facebook) && (vdLower.contains(status) || vdLower.contains(post))) {
                if (vdLower.contains(status)) {
                    String[] separated = vdLower.split(status);
                    if (separated.length > 1) {
                        String action = separated[1].trim();
                        if (action.startsWith(saying)) {
                            commandFacebookValues.setText(action.replaceFirst(saying, "").trim());
                        } else {
                            commandFacebookValues.setText(action);
                        }
                        commandFacebookValues.setResolved(true);
                    } else {
                        commandFacebookValues.setResolved(false);
                    }
                } else {
                    String[] separated = vdLower.split(post);
                    if (separated.length <= 1) {
                        commandFacebookValues.setResolved(false);
                    } else if (separated[1].trim().contains(facebook)) {
                        separated = vdLower.split(facebook);
                        if (separated.length > 1) {
                            String action = separated[1].trim();
                            if (action.startsWith(saying)) {
                                commandFacebookValues.setText(action.replaceFirst(saying, "").trim());
                            } else {
                                commandFacebookValues.setText(action);
                            }
                            commandFacebookValues.setResolved(true);
                        } else {
                            commandFacebookValues.setResolved(false);
                        }
                    } else {
                        commandFacebookValues.setResolved(false);
                    }
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return commandFacebookValues;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        facebook = sr.getString(R.string.facebook);
        status = sr.getString(R.string.status);
        post = sr.getString(R.string.post);
        saying = sr.getString(R.string.saying);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        final ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(voiceData) && ai.saiy.android.utils.UtilsList.notNaked(confidence) && voiceData.size() == confidence.length) {
            final Locale locale = sl.getLocale();
            int size = voiceData.size();
            String vdLower;
            for (int i = 0; i < size; i++) {
                vdLower = voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.matches(facebook) || (vdLower.contains(facebook) && (vdLower.contains(status) || vdLower.contains(post)))) {
                    toReturn.add(new Pair<>(CC.COMMAND_FACEBOOK, confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "facebook: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
