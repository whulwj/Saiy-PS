package ai.saiy.android.command.twitter;

import android.content.Context;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.utils.MyLog;

public class Twitter_en {
    private static final boolean DEBUG = ai.saiy.android.utils.MyLog.DEBUG;
    private static final String CLS_NAME = Twitter_en.class.getSimpleName();
    private static final byte MIN_SIZE = 5;

    private static String tweet;
    private static String word_new;
    private static String post;
    private static String send;
    private static String twitter;
    private static String compose;
    private static String status;
    private static String update;
    private static String saying;
    private final SupportedLanguage sl;
    private final ArrayList<String> voiceData;
    private final float[] confidence;

    public Twitter_en(ai.saiy.android.localisation.SaiyResources sr, SupportedLanguage supportedLanguage, ArrayList<String> voiceData, float[] confidence) {
        this.sl = supportedLanguage;
        this.voiceData = voiceData;
        this.confidence = confidence;
        if (tweet == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "initialising strings");
            }
            initStrings(sr);
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "strings initialised");
        }
    }

    public static CommandTwitterValues sortTwitter(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Locale locale = supportedLanguage.getLocale();
        final CommandTwitterValues commandTwitterValues = new CommandTwitterValues();
        commandTwitterValues.setResolved(false);
        commandTwitterValues.setText("");
        if (tweet == null) {
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
            if (vdLower.matches(tweet) || vdLower.matches(twitter)) {
                break;
            }
            if (vdLower.startsWith(tweet) || vdLower.startsWith(twitter) || vdLower.startsWith(word_new) || vdLower.startsWith(compose) || vdLower.startsWith(send) || vdLower.startsWith(post) || vdLower.startsWith(update)) {
                if (vdLower.contains(twitter) || vdLower.contains(tweet)) {
                    if (vdLower.contains(twitter)) {
                        String[] separated = vdLower.split(twitter);
                        if (separated.length <= 1) {
                            commandTwitterValues.setResolved(false);
                            break;
                        }
                        String action = separated[1].trim();
                        if (action.contains(tweet)) {
                            separated = vdLower.split(tweet);
                            if (separated.length > 1) {
                                action = separated[1].trim();
                                if (ai.saiy.android.utils.UtilsString.stripSpace(action) <= MIN_SIZE) {
                                    commandTwitterValues.setResolved(false);
                                } else if (action.startsWith(saying)) {
                                    commandTwitterValues.setText(action.replaceFirst(saying, "").trim());
                                    commandTwitterValues.setResolved(true);
                                } else {
                                    commandTwitterValues.setText(action);
                                    commandTwitterValues.setResolved(true);
                                }
                            } else {
                                commandTwitterValues.setResolved(false);
                            }
                        } else if (ai.saiy.android.utils.UtilsString.stripSpace(action) > MIN_SIZE) {
                            if (action.startsWith(saying)) {
                                commandTwitterValues.setText(action.replaceFirst(saying, "").trim());
                                commandTwitterValues.setResolved(true);
                            } else if (action.startsWith(post)) {
                                commandTwitterValues.setText(action.replaceFirst(post, "").trim());
                                commandTwitterValues.setResolved(true);
                            } else if (action.startsWith(status)) {
                                commandTwitterValues.setText(action.replaceFirst(status, "").trim());
                                commandTwitterValues.setResolved(true);
                            } else if (action.startsWith(update)) {
                                commandTwitterValues.setText(action.replaceFirst(update, "").trim());
                                commandTwitterValues.setResolved(true);
                            } else {
                                commandTwitterValues.setText(action);
                                commandTwitterValues.setResolved(true);
                            }
                        }
                    } else {
                        String[] separated = vdLower.split(tweet);
                        if (separated.length > 1) {
                            String action = separated[1].trim();
                            if (ai.saiy.android.utils.UtilsString.stripSpace(action) <= MIN_SIZE) {
                                commandTwitterValues.setResolved(false);
                            } else if (action.startsWith(saying)) {
                                commandTwitterValues.setText(action.replaceFirst(saying, "").trim());
                                commandTwitterValues.setResolved(true);
                            } else {
                                commandTwitterValues.setText(action);
                                commandTwitterValues.setResolved(true);
                            }
                        } else {
                            commandTwitterValues.setResolved(false);
                        }
                    }
                }
            }
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return commandTwitterValues;
    }

    private static void initStrings(ai.saiy.android.localisation.SaiyResources sr) {
        tweet = sr.getString(R.string.tweet);
        word_new = sr.getString(R.string.word_new);
        post = sr.getString(R.string.post);
        send = sr.getString(R.string.send);
        status = sr.getString(R.string.status);
        twitter = sr.getString(R.string.twitter);
        compose = sr.getString(R.string.compose);
        saying = sr.getString(R.string.saying);
        update = sr.getString(R.string.update);
    }

    public ArrayList<Pair<CC, Float>> detectCallable() {
        final long then = System.nanoTime();
        ArrayList<Pair<CC, Float>> toReturn = new ArrayList<>();
        if (ai.saiy.android.utils.UtilsList.notNaked(this.voiceData) && ai.saiy.android.utils.UtilsList.notNaked(this.confidence) && this.voiceData.size() == this.confidence.length) {
            final Locale locale = this.sl.getLocale();
            final int size = this.voiceData.size();
            String vdLower;
            for (int i = 0; i < size; i++) {
                vdLower = this.voiceData.get(i).toLowerCase(locale).trim();
                if (vdLower.startsWith(tweet) || vdLower.startsWith(twitter)) {
                    toReturn.add(new Pair<>(CC.COMMAND_TWITTER, this.confidence[i]));
                } else if ((vdLower.startsWith(word_new) || vdLower.startsWith(compose) || vdLower.startsWith(send) || vdLower.startsWith(post) || vdLower.startsWith(update)) && (vdLower.contains(twitter) || vdLower.contains(tweet))) {
                    toReturn.add(new Pair<>(CC.COMMAND_TWITTER, this.confidence[i]));
                }
            }
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "Twitter: returning ~ " + toReturn.size());
            MyLog.getElapsed(CLS_NAME, then);
        }
        return toReturn;
    }
}
