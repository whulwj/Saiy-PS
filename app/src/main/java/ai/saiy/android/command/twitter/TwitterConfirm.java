package ai.saiy.android.command.twitter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.command.search.provider.TwitterHelper;
import ai.saiy.android.firebase.database.reference.TwitterReference;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.PositiveNegative;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;
import twitter4j.Twitter;
import twitter4j.v1.Status;
import twitter4j.TwitterException;

public class TwitterConfirm {
    private static final boolean DEBUG = ai.saiy.android.utils.MyLog.DEBUG;
    private final String CLS_NAME = TwitterConfirm.class.getSimpleName();

    private final Context mContext;
    private final Bundle bundle;
    private final ContentType contentType;
    private final Locale vrLocale;
    private final Locale ttsLocale;
    private final SupportedLanguage sl;
    private final ArrayList<String> resultsRecognition;

    public enum ContentType {
        TWITTER,
        TWITTER_CONTENT
    }

    public TwitterConfirm(Context context, Bundle bundle, ContentType contentType, Locale vrLocale, Locale ttsLocale) {
        this.mContext = context;
        this.bundle = bundle;
        this.contentType = contentType;
        this.vrLocale = vrLocale;
        this.ttsLocale = ttsLocale;
        this.sl = (SupportedLanguage) bundle.getSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE);
        this.resultsRecognition = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    }

    public void confirm() {
        LocalRequest localRequest;
        CommandTwitterValues commandTwitterValues;
        switch (contentType) {
            case TWITTER:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "TWITTER");
                }
                final PositiveNegative positiveNegative = new PositiveNegative();
                final PositiveNegative.Result result = positiveNegative.resolve(mContext, resultsRecognition, sl);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "result: " + result.name());
                    MyLog.i(CLS_NAME, "confidence: " + positiveNegative.getConfidence());
                }
                switch (result) {
                    case UNRESOLVED:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "UNRESOLVED");
                        }
                        if (!bundle.containsKey(LocalRequest.EXTRA_VR_RETRY)) {
                            bundle.putBoolean(LocalRequest.EXTRA_VR_RETRY, true);
                            localRequest = new LocalRequest(mContext, bundle);
                            localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_LISTEN, sl, vrLocale, ttsLocale, PersonalityResponse.getCallConfirmationRepeat(mContext, sl));
                            localRequest.execute();
                            break;
                        }
                        localRequest = new LocalRequest(mContext);
                        localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getTwitterConfirmationMisheard(mContext, sl));
                        TwitterHelper.shareIntent(mContext, ((CommandTwitterValues) bundle.getParcelable(LocalRequest.EXTRA_OBJECT)).getText());
                        localRequest.execute();
                        break;
                    case POSITIVE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "POSITIVE");
                        }
                        TwitterHelper.shareIntent(mContext, ((CommandTwitterValues) bundle.getParcelable(LocalRequest.EXTRA_OBJECT)).getText());
                        localRequest = new LocalRequest(mContext, bundle);
                        localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getMessageProofReadAcknowledge(mContext, sl));
                        localRequest.execute();
                        break;
                    case NEGATIVE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "NEGATIVE");
                        }
                        commandTwitterValues = bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getText: " + commandTwitterValues.getText());
                        }
                        localRequest = new LocalRequest(mContext, bundle);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final Pair<Boolean, Pair<String, String>> twitterPair = new TwitterReference().getCredentials(mContext);
                                    if (twitterPair.first) {
                                        final Twitter twitter = Twitter.newBuilder().oAuthConsumer(twitterPair.second.first, twitterPair.second.second)
                                                .oAuthAccessToken(ai.saiy.android.utils.SPH.getTwitterToken(mContext), ai.saiy.android.utils.SPH.getTwitterSecret(mContext)).build();
                                        final Status updateStatus = twitter.v1().tweets().updateStatus(commandTwitterValues.getText());
                                        if (updateStatus == null || updateStatus.getText() == null) {
                                            if (DEBUG) {
                                                MyLog.w(CLS_NAME, "response or text null");
                                            }
                                            localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getTwitterPostError(mContext, sl));
                                            TwitterHelper.shareIntent(mContext, commandTwitterValues.getText());
                                        } else {
                                            if (DEBUG) {
                                                MyLog.i(CLS_NAME, "response: " + updateStatus.getText());
                                            }
                                            localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getTwitterAcknowledge(mContext, sl));
                                        }
                                        localRequest.execute();
                                        return;
                                    }
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "twitterPair error");
                                    }
                                    localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getTwitterPostError(mContext, sl));
                                    TwitterHelper.shareIntent(mContext, commandTwitterValues.getText());
                                    localRequest.execute();
                                } catch (NullPointerException | TwitterException e) {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "TwitterException/NullPointerException");
                                        e.printStackTrace();
                                    }
                                    localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getTwitterPostError(mContext, sl));
                                    TwitterHelper.shareIntent(mContext, commandTwitterValues.getText());
                                    localRequest.execute();
                                } catch (Exception e) {
                                    if (DEBUG) {
                                        MyLog.w(CLS_NAME, "Exception");
                                        e.printStackTrace();
                                    }
                                    localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, PersonalityResponse.getTwitterPostError(mContext, sl));
                                    TwitterHelper.shareIntent(mContext, commandTwitterValues.getText());
                                    localRequest.execute();
                                }
                            }
                        });
                        break;
                    case CANCEL:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "CANCEL");
                        }
                        localRequest = new LocalRequest(mContext);
                        localRequest.prepareCancelled(sl, vrLocale, ttsLocale);
                        localRequest.execute();
                        break;
                    default:
                        break;
                }
                break;
            case TWITTER_CONTENT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "TWITTER_CONTENT");
                }
                if (new ai.saiy.android.command.cancel.Cancel(sl, new ai.saiy.android.localisation.SaiyResources(mContext, sl), true).detectCancel(resultsRecognition)) {
                    localRequest = new LocalRequest(mContext);
                    localRequest.prepareCancelled(sl, vrLocale, ttsLocale);
                    localRequest.execute();
                    break;
                }
                localRequest = new LocalRequest(mContext, bundle);
                commandTwitterValues = bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
                commandTwitterValues.setContentType(ContentType.TWITTER);
                commandTwitterValues.setText(UtilsString.convertProperCase(resultsRecognition.get(0), sl.getLocale()));
                localRequest.setParcelableObject(commandTwitterValues);
                localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_LISTEN, sl, vrLocale, ttsLocale, PersonalityResponse.getTwitterConfirmation(mContext, sl, commandTwitterValues.getText()));
                localRequest.setCondition(Condition.CONDITION_TWITTER);
                localRequest.execute();
                break;
            default:
                break;
        }
    }
}
