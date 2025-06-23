package ai.saiy.android.command.facebook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;

import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Locale;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.PositiveNegative;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityFacebook;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class FacebookConfirm {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FacebookConfirm.class.getSimpleName();

    private final Context mContext;
    private final Bundle bundle;
    private final ContentType contentType;
    private final Locale vrLocale;
    private final Locale ttsLocale;
    private final SupportedLanguage sl;
    private final ArrayList<String> resultsRecognition;

    public enum ContentType {
        FACEBOOK,
        FACEBOOK_CONTENT
    }

    public FacebookConfirm(Context context, Bundle bundle, ContentType contentType, Locale vrLocale, Locale ttsLocale) {
        this.mContext = context;
        this.bundle = bundle;
        this.contentType = contentType;
        this.vrLocale = vrLocale;
        this.ttsLocale = ttsLocale;
        this.sl = (SupportedLanguage) bundle.getSerializable(LocalRequest.EXTRA_SUPPORTED_LANGUAGE);
        this.resultsRecognition = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    }

    public void confirm() {
        CommandFacebookValues commandFacebookValues;
        ai.saiy.android.service.helper.LocalRequest localRequest;
        Bundle actionBundle;
        switch (contentType) {
            case FACEBOOK:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "FACEBOOK");
                }
                final PositiveNegative positiveNegative = new PositiveNegative();
                final PositiveNegative.Result result = positiveNegative.resolve(mContext, resultsRecognition, sl);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "result: " + result.name());
                    MyLog.i(CLS_NAME, "confidence: " + positiveNegative.getConfidence());
                }
                String utterance;
                switch (result) {
                    case UNRESOLVED:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "UNRESOLVED");
                        }
                        if (ai.saiy.android.utils.SPH.getFacebookCommandVerbose(mContext) >= 2) {
                            utterance = "";
                        } else {
                            ai.saiy.android.utils.SPH.setFacebookCommandVerbose(mContext);
                            utterance = ai.saiy.android.personality.PersonalityResponse.getFacebookVerbose(mContext, sl);
                        }
                        if (!this.bundle.containsKey(LocalRequest.EXTRA_VR_RETRY)) {
                            this.bundle.putBoolean(LocalRequest.EXTRA_VR_RETRY, true);
                            localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext, this.bundle);
                            localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_LISTEN, sl, vrLocale, ttsLocale, ai.saiy.android.personality.PersonalityResponse.getCallConfirmationRepeat(mContext, sl));
                            localRequest.execute();
                            break;
                        }
                        localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext);
                        localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, ai.saiy.android.personality.PersonalityResponse.FacebookConfirmationMisheard(mContext, sl) + XMLResultsHandler.SEP_SPACE + utterance);
                        commandFacebookValues = this.bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
                        actionBundle = new Bundle();
                        actionBundle.putInt(ActivityFacebook.EXTRA_REQUEST_TYPE, ActivityFacebook.TYPE_DIALOG);
                        actionBundle.putString(Intent.EXTRA_TEXT, commandFacebookValues.getText());
                        ai.saiy.android.intent.ExecuteIntent.saiyActivity(mContext, ActivityFacebook.class, actionBundle, true);
                        localRequest.execute();
                        break;
                    case POSITIVE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "POSITIVE");
                        }
                        if (ai.saiy.android.utils.SPH.getFacebookCommandVerbose(mContext) >= 2) {
                            utterance = "";
                        } else {
                            ai.saiy.android.utils.SPH.setFacebookCommandVerbose(mContext);
                            utterance = ai.saiy.android.personality.PersonalityResponse.getFacebookVerbose(mContext, sl);
                        }
                        commandFacebookValues = this.bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
                        actionBundle = new Bundle();
                        actionBundle.putInt(ActivityFacebook.EXTRA_REQUEST_TYPE, ActivityFacebook.TYPE_DIALOG);
                        actionBundle.putString(Intent.EXTRA_TEXT, commandFacebookValues.getText());
                        ai.saiy.android.intent.ExecuteIntent.saiyActivity(mContext, ActivityFacebook.class, actionBundle, true);
                        localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext, this.bundle);
                        localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, ai.saiy.android.personality.PersonalityResponse.getMessageProofReadAcknowledge(mContext, sl) + XMLResultsHandler.SEP_SPACE + utterance);
                        localRequest.execute();
                        break;
                    case NEGATIVE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "NEGATIVE");
                        }
                        commandFacebookValues = this.bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "getText: " + commandFacebookValues.getText());
                        }
                        localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext, this.bundle);
                        final com.facebook.share.ShareApi shareApi = new com.facebook.share.ShareApi(new ShareLinkContent.Builder().build());
                        shareApi.setMessage(commandFacebookValues.getText());
                        shareApi.share(new com.facebook.FacebookCallback<Sharer.Result>() {
                            @Override
                            public void onError(@NonNull FacebookException facebookException) {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "onError");
                                    facebookException.printStackTrace();
                                }
                                String utterance;
                                if (ai.saiy.android.utils.SPH.getFacebookCommandVerbose(mContext) >= 2) {
                                    utterance = "";
                                } else {
                                    ai.saiy.android.utils.SPH.setFacebookCommandVerbose(mContext);
                                    utterance = ai.saiy.android.personality.PersonalityResponse.getFacebookVerbose(mContext, sl);
                                }
                                localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, ai.saiy.android.personality.PersonalityResponse.getFacebookPostError(mContext, sl) + XMLResultsHandler.SEP_SPACE + utterance);
                                final Bundle actionBundle = new Bundle();
                                actionBundle.putInt(ActivityFacebook.EXTRA_REQUEST_TYPE, ActivityFacebook.TYPE_DIALOG);
                                actionBundle.putString(Intent.EXTRA_TEXT, commandFacebookValues.getText());
                                ai.saiy.android.intent.ExecuteIntent.saiyActivity(mContext, ActivityFacebook.class, actionBundle, true);
                                localRequest.execute();
                            }

                            @Override
                            public void onSuccess(Sharer.Result result) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onSuccess");
                                }
                                localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, ai.saiy.android.personality.PersonalityResponse.getFacebookAcknowledge(mContext, sl));
                                localRequest.execute();
                            }

                            @Override
                            public void onCancel() {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "onCancel");
                                }
                                String utterance;
                                if (ai.saiy.android.utils.SPH.getFacebookCommandVerbose(mContext) >= 2) {
                                    utterance = "";
                                } else {
                                    ai.saiy.android.utils.SPH.setFacebookCommandVerbose(mContext);
                                    utterance = ai.saiy.android.personality.PersonalityResponse.getFacebookVerbose(mContext, sl);
                                }
                                localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_ONLY, sl, vrLocale, ttsLocale, ai.saiy.android.personality.PersonalityResponse.getFacebookPostError(mContext, sl) + XMLResultsHandler.SEP_SPACE + utterance);
                                final Bundle actionBundle = new Bundle();
                                actionBundle.putInt(ActivityFacebook.EXTRA_REQUEST_TYPE, ActivityFacebook.TYPE_DIALOG);
                                actionBundle.putString(Intent.EXTRA_TEXT, commandFacebookValues.getText());
                                ai.saiy.android.intent.ExecuteIntent.saiyActivity(mContext, ActivityFacebook.class, actionBundle, true);
                                localRequest.execute();
                            }
                        });
                        break;
                    case CANCEL:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "CANCEL");
                        }
                        localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext);
                        localRequest.prepareCancelled(sl, vrLocale, ttsLocale);
                        localRequest.execute();
                        break;
                    default:
                        break;
                }
                break;
            case FACEBOOK_CONTENT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "FACEBOOK_CONTENT");
                }
                if (new ai.saiy.android.command.cancel.Cancel(sl, new ai.saiy.android.localisation.SaiyResources(mContext, sl)).detectCancel(resultsRecognition)) {
                    localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext);
                    localRequest.prepareCancelled(sl, vrLocale, ttsLocale);
                    localRequest.execute();
                    break;
                }
                localRequest = new ai.saiy.android.service.helper.LocalRequest(mContext, bundle);
                commandFacebookValues = bundle.getParcelable(LocalRequest.EXTRA_OBJECT);
                commandFacebookValues.setContentType(ContentType.FACEBOOK);
                commandFacebookValues.setText(UtilsString.convertProperCase(resultsRecognition.get(0), sl.getLocale()));
                localRequest.setParcelableObject(commandFacebookValues);
                localRequest.prepareDefault(LocalRequest.ACTION_SPEAK_LISTEN, sl, vrLocale, ttsLocale, ai.saiy.android.personality.PersonalityResponse.getFacebookConfirmation(mContext, sl, commandFacebookValues.getText()));
                localRequest.setCondition(Condition.CONDITION_FACEBOOK);
                localRequest.execute();
                break;
            default:
                break;
        }
    }
}
