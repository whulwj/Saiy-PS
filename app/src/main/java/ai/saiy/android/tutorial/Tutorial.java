package ai.saiy.android.tutorial;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.SpeechRecognizer;
import android.util.Pair;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import ai.saiy.android.R;
import ai.saiy.android.intent.ExecuteIntent;
import ai.saiy.android.localisation.SaiyResourcesHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.nlu.local.PositiveNegative;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.service.helper.SelfAwareHelper;
import ai.saiy.android.tts.attributes.Gender;
import ai.saiy.android.tts.helper.SpeechPriority;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.notification.NotificationHelper;
import ai.saiy.android.utils.Global;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.firebase.helper.UtilsAnalytic;
import ai.saiy.android.utils.UtilsString;

public class Tutorial {
    public static final int TUTORIAL_WINDOW_ID = 2;
    private static final byte STAGE_INVALID = -1;
    public static final byte STAGE_INTRO = 1;
    private static final byte STAGE_INTRO_2 = 2;
    private static final byte STAGE_COMMANDS = 3;
    private static final byte STAGE_COMMANDS_1 = 4;
    private static final byte STAGE_COMMANDS_2 = 5;
    private static final byte STAGE_QUESTION_1 = 6;
    private static final byte STAGE_ANSWER_1 = 7;
    private static final byte STAGE_QUESTION_2 = 8;
    private static final byte STAGE_ANSWER_2 = 9;
    private static final byte STAGE_QUESTION_3 = 10;
    private static final byte STAGE_ANSWER_3 = 11;
    private static final byte STAGE_QUESTION_4 = 12;
    private static final byte STAGE_ANSWER_4 = 13;
    private static final byte STAGE_ANSWER_5 = 14;
    private static final byte STAGE_CUSTOMISATION = 15;
    private static final byte STAGE_CUSTOMISATION_1 = 16;
    private static final byte STAGE_SETTINGS = 17;
    private static final byte STAGE_ADVANCED_SETTINGS = 18;
    private static final byte STAGE_APPLICATIONS = 19;
    private static final byte STAGE_DEVELOPMENT = 20;
    private static final byte STAGE_FINAL = 21;
    private static final byte STAGE_RESET = 22;
    public static final byte STAGE_RESET_FOR_ERROR = 23;

    private static int sErrorCount;

    boolean vrRetry;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = Tutorial.class.getSimpleName();
    private final Context context;
    private final ArrayList<String> resultsRecognition;
    private final Locale vrLocale;
    private final Locale ttsLocale;
    private final SupportedLanguage supportedLanguage;
    private final Bundle bundle;
    private final TutorialHelper helper;

    public Tutorial(Context context, Locale vrLocale, Locale ttsLocale, SupportedLanguage supportedLanguage, Bundle bundle) {
        this.context = context.getApplicationContext();
        this.bundle = bundle;
        this.resultsRecognition = this.bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        this.vrLocale = vrLocale;
        this.ttsLocale = ttsLocale;
        this.supportedLanguage = supportedLanguage;
        this.helper = new TutorialHelper(this.context, this.supportedLanguage, this.resultsRecognition);
    }

    private void executeRequest(int action, int tutorialStage, String utterance, boolean conditionRetry) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "executeRequest nextStage: " + tutorialStage);
        }
        ai.saiy.android.service.helper.LocalRequest localRequest = new ai.saiy.android.service.helper.LocalRequest(this.context);
        localRequest.prepareDefault(action, this.supportedLanguage, this.vrLocale, this.ttsLocale, utterance);
        localRequest.setConditionRetry(conditionRetry);
        localRequest.setVrRetry(this.vrRetry);
        if (tutorialStage > STAGE_INVALID) {
            localRequest.setCondition(Condition.CONDITION_TUTORIAL);
            localRequest.setTutorialStage(tutorialStage);
            localRequest.setSpeechPriority(SpeechPriority.PRIORITY_TUTORIAL);
        }
        localRequest.execute();
    }

    public void execute() {
        if (!Global.isInVoiceTutorial()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "tutorialActive: false. Must have been shutdown");
            }
            return;
        }

        this.vrRetry = this.bundle.getBoolean(LocalRequest.EXTRA_VR_RETRY, false);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "vrRetry: " + this.vrRetry);
        }
        int tutorialStage = this.bundle.getInt(LocalRequest.EXTRA_TUTORIAL_STAGE, STAGE_INTRO);
        if (this.vrRetry && tutorialStage > STAGE_INTRO) {
            --tutorialStage;
        }

        int nextAction;
        int nextTutorialStage;
        boolean conditionRetry = false;
        String utterance;
        Bundle actionBundle = new Bundle();
        switch (tutorialStage) {
            case STAGE_INTRO:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_INTRO");
                }
                sErrorCount = 0;
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_INTRO_2, SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_1), false);
                break;
            case STAGE_INTRO_2:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_INTRO_2");
                }
                actionBundle.putInt(ActivityHome.FRAGMENT_INDEX, ActivityHome.INDEX_FRAGMENT_COMMANDS);
                actionBundle.putInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_TUTORIAL);
                ExecuteIntent.saiyActivity(context, ActivityHome.class, actionBundle, true);
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_COMMANDS, SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_2) + " [tick]", false);
                break;
            case STAGE_COMMANDS:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_COMMANDS");
                }
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_COMMANDS_1, SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_3), false);
                break;
            case STAGE_COMMANDS_1:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_COMMANDS_1");
                }
                SelfAwareHelper.startFloatingService(context, TUTORIAL_WINDOW_ID);
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_COMMANDS_2, SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_4), false);
                break;
            case STAGE_COMMANDS_2:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_COMMANDS_2");
                }
                SelfAwareHelper.stopFloatingService(context);
                ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(125L);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        NotificationHelper.createListeningNotification(Tutorial.this.context);
                    }
                }, 1000L);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        NotificationHelper.cancelListeningNotification(Tutorial.this.context);
                    }
                }, 2000L);
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_QUESTION_1, SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_5), false);
                break;
            case STAGE_QUESTION_1:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_QUESTION_1: vrRetry: " + this.vrRetry);
                }
                executeRequest(LocalRequest.ACTION_SPEAK_LISTEN, STAGE_ANSWER_1, this.vrRetry ? SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_6b) + XMLResultsHandler.SEP_SPACE + SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_6) : SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_6), false);
                break;
            case STAGE_ANSWER_1:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_ANSWER_1");
                }
                Pair<Boolean, Boolean> answerOneResult = helper.getAnswerOneResult();
                if (!(Boolean) answerOneResult.first) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "action: STAGE_ANSWER_1: failure");
                    }
                    sErrorCount++;
                    utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_7b);
                } else if (answerOneResult.second) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "action: STAGE_ANSWER_1: perfect");
                    }
                    utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_7a);
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "action: STAGE_ANSWER_1: acceptable");
                    }
                    utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_7e);
                }
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_QUESTION_2, utterance, false);
                break;
            case STAGE_QUESTION_2:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_QUESTION_2");
                }
                executeRequest(LocalRequest.ACTION_SPEAK_LISTEN, STAGE_ANSWER_2, this.vrRetry ? SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_7f) + XMLResultsHandler.SEP_SPACE + SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_7c) : SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_7c), false);
                break;
            case STAGE_ANSWER_2:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_ANSWER_2");
                }
                Pair<Boolean, Boolean> answerTwoResult = helper.getAnswerTwoResult();
                if (!(Boolean) answerTwoResult.first) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "action: STAGE_ANSWER_2: failure");
                    }
                    sErrorCount++;
                    utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_8b);
                } else if (answerTwoResult.second) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "action: STAGE_ANSWER_2: perfect");
                    }
                    utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_8a);
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "action: STAGE_ANSWER_2: acceptable");
                    }
                    utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_8e);
                }
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_QUESTION_3, utterance, false);
                break;
            case STAGE_QUESTION_3:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_QUESTION_3");
                }
                conditionRetry = this.bundle.getBoolean(LocalRequest.EXTRA_CONDITION_RETRY, false);
                executeRequest(LocalRequest.ACTION_SPEAK_LISTEN, STAGE_ANSWER_3, this.vrRetry ? SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_8d) + XMLResultsHandler.SEP_SPACE + SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_8c) : SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_8c), conditionRetry);
                break;
            case STAGE_ANSWER_3:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_ANSWER_3");
                }
                Pair<Gender, Boolean> answerThreeResult = helper.getAnswerThreeResult();
                switch (answerThreeResult.first) {
                    case MALE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "action: STAGE_ANSWER_3: MALE");
                        }
                        SPH.setUserGender(context, Gender.MALE);
                        utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_9a);
                        nextTutorialStage = STAGE_QUESTION_4;
                        break;
                    case FEMALE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "action: STAGE_ANSWER_3: FEMALE");
                        }
                        SPH.setUserGender(context, Gender.FEMALE);
                        utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_9b);
                        nextTutorialStage = STAGE_QUESTION_4;
                        break;
                    default:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "action: STAGE_ANSWER_3: UNDEFINED");
                        }
                        if (!(Boolean) answerThreeResult.second) {
                            sErrorCount++;
                            if (!this.bundle.getBoolean(LocalRequest.EXTRA_CONDITION_RETRY, false)) {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "action: STAGE_ANSWER_3: UNDEFINED: retrying");
                                }
                                utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_9f);
                                nextTutorialStage = STAGE_QUESTION_3;
                                conditionRetry = true;
                                break;
                            } else {
                                if (DEBUG) {
                                    MyLog.i(CLS_NAME, "action: STAGE_ANSWER_3: UNDEFINED: already retried");
                                }
                                utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_9h);
                                nextTutorialStage = STAGE_QUESTION_4;
                                break;
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "action: STAGE_ANSWER_3: UNDEFINED: sarcasm");
                            }
                            utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_9d);
                            nextTutorialStage = STAGE_QUESTION_4;
                            break;
                        }
                }
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, nextTutorialStage, utterance, conditionRetry);
                break;
            case STAGE_QUESTION_4:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_QUESTION_4");
                }
                executeRequest(LocalRequest.ACTION_SPEAK_LISTEN, STAGE_ANSWER_4, this.vrRetry ? SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_11e) + XMLResultsHandler.SEP_SPACE + SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_11d) : SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_9c), false);
                break;
            case STAGE_ANSWER_4:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_ANSWER_4");
                }
                nextAction = LocalRequest.ACTION_SPEAK_ONLY;
                String userName = helper.detectCallable();
                if (UtilsString.notNaked(userName)) {
                    SPH.setUserName(context, userName);
                    utterance = String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_10a), userName);
                    nextTutorialStage = STAGE_ANSWER_5;
                    conditionRetry = this.bundle.getBoolean(LocalRequest.EXTRA_CONDITION_RETRY, false);
                    nextAction = LocalRequest.ACTION_SPEAK_LISTEN;
                } else if (this.vrRetry) {
                    String oldUserName = SPH.getUserName(context);
                    if ((UtilsString.notNaked(oldUserName) && !oldUserName.matches(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.master)))) {
                        utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_10e) + XMLResultsHandler.SEP_SPACE + String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_10a), oldUserName);
                        nextTutorialStage = STAGE_ANSWER_5;
                    } else {
                        conditionRetry = true;
                        nextTutorialStage = STAGE_ANSWER_4;
                        utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_11f) + XMLResultsHandler.SEP_SPACE + SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_11d);
                    }
                    nextAction = LocalRequest.ACTION_SPEAK_LISTEN;
                } else {
                    sErrorCount++;
                    if (this.bundle.getBoolean(LocalRequest.EXTRA_CONDITION_RETRY, false)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "action: STAGE_ANSWER_4: naked: already retried");
                        }
                        utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_10d);
                        nextTutorialStage = STAGE_CUSTOMISATION;
                    } else {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "action: STAGE_ANSWER_4: naked: retrying");
                        }
                        conditionRetry = true;
                        nextAction = LocalRequest.ACTION_SPEAK_LISTEN;
                        nextTutorialStage = STAGE_ANSWER_4;
                        utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_10c);
                    }
                }
                executeRequest(nextAction, nextTutorialStage, utterance, conditionRetry);
                break;
            case STAGE_ANSWER_5:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_ANSWER_5");
                }
                nextTutorialStage = STAGE_ANSWER_4;
                nextAction = LocalRequest.ACTION_SPEAK_ONLY;
                PositiveNegative positiveNegative = new PositiveNegative();
                PositiveNegative.Result result = positiveNegative.resolve(context, resultsRecognition, supportedLanguage);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "result: " + result.name());
                    MyLog.i(CLS_NAME, "confidence: " + positiveNegative.getConfidence());
                }
                switch (result) {
                    case UNRESOLVED:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "STAGE_ANSWER_5: UNRESOLVED");
                        }
                        sErrorCount++;
                        if (!this.bundle.getBoolean(LocalRequest.EXTRA_CONDITION_RETRY, false)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "action: STAGE_ANSWER_5: UNRESOLVED: retrying");
                            }
                            utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_11b);
                            conditionRetry = true;
                            nextAction = LocalRequest.ACTION_SPEAK_LISTEN;
                            break;
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "action: STAGE_ANSWER_5: UNRESOLVED: already retried");
                            }
                            utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_11c);
                            nextTutorialStage = STAGE_CUSTOMISATION;
                            break;
                        }
                    case POSITIVE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "STAGE_ANSWER_5: POSITIVE");
                        }
                        utterance = String.format(SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_11a), SPH.getUserName(context));
                        nextTutorialStage = STAGE_CUSTOMISATION;
                        conditionRetry = true;
                        break;
                    case NEGATIVE:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "STAGE_ANSWER_5: NEGATIVE");
                        }
                        sErrorCount++;
                        if (!this.bundle.getBoolean(LocalRequest.EXTRA_CONDITION_RETRY, false)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "action: STAGE_ANSWER_5: NEGATIVE: retrying");
                            }
                            utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_11b);
                            conditionRetry = true;
                            nextAction = LocalRequest.ACTION_SPEAK_LISTEN;
                            break;
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "action: STAGE_ANSWER_5: NEGATIVE: already retried");
                            }
                            utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_11c);
                            nextTutorialStage = STAGE_CUSTOMISATION;
                            break;
                        }
                    default:
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "STAGE_ANSWER_5: CANCEL");
                        }
                        sErrorCount++;
                        if (!this.bundle.getBoolean(LocalRequest.EXTRA_CONDITION_RETRY, false)) {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "action: STAGE_ANSWER_5: CANCEL: retrying");
                            }
                            utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_11b);
                            conditionRetry = true;
                            nextAction = LocalRequest.ACTION_SPEAK_LISTEN;
                            break;
                        } else {
                            if (DEBUG) {
                                MyLog.i(CLS_NAME, "action: STAGE_ANSWER_5: CANCEL: already retried");
                            }
                            utterance = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_11c);
                            nextTutorialStage = STAGE_CUSTOMISATION;
                            break;
                        }
                }
                executeRequest(nextAction, nextTutorialStage, utterance, conditionRetry);
                break;
            case STAGE_CUSTOMISATION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_CUSTOMISATION");
                }
                actionBundle.putInt(ActivityHome.FRAGMENT_INDEX, ActivityHome.INDEX_FRAGMENT_CUSTOMISATION);
                actionBundle.putInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_TUTORIAL);
                ExecuteIntent.saiyActivity(context, ActivityHome.class, actionBundle, true);
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_CUSTOMISATION_1, SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_12), false);
                break;
            case STAGE_CUSTOMISATION_1:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_CUSTOMISATION_1");
                }
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_SETTINGS, SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_13), false);
                break;
            case STAGE_SETTINGS:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_SETTINGS");
                }
                actionBundle.putInt(ActivityHome.FRAGMENT_INDEX, ActivityHome.INDEX_FRAGMENT_SETTINGS);
                actionBundle.putInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_TUTORIAL);
                ExecuteIntent.saiyActivity(context, ActivityHome.class, actionBundle, true);
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_ADVANCED_SETTINGS, SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_14), false);
                break;
            case STAGE_ADVANCED_SETTINGS:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_ADVANCED_SETTINGS");
                }
                actionBundle.putInt(ActivityHome.FRAGMENT_INDEX, ActivityHome.INDEX_FRAGMENT_ADVANCED_SETTINGS);
                actionBundle.putInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_TUTORIAL);
                ExecuteIntent.saiyActivity(context, ActivityHome.class, actionBundle, true);
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_APPLICATIONS, SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_15), false);
                break;
            case STAGE_APPLICATIONS:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_APPLICATIONS");
                }
                actionBundle.putInt(ActivityHome.FRAGMENT_INDEX, ActivityHome.INDEX_FRAGMENT_SUPPORTED_APPS);
                actionBundle.putInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_TUTORIAL);
                ExecuteIntent.saiyActivity(context, ActivityHome.class, actionBundle, true);
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_DEVELOPMENT, SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_16) + XMLResultsHandler.SEP_SPACE + SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_17), false);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Bundle innerBundle = new Bundle();
                        innerBundle.putInt(ActivityHome.DIALOG_INDEX, ActivityHome.INDEX_DIALOG_USER_GUIDE);
                        ExecuteIntent.saiyActivity(Tutorial.this.context, ActivityHome.class, innerBundle, true);
                    }
                }, 25 * 1000L);
                break;
            case STAGE_DEVELOPMENT:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_DEVELOPMENT");
                }
                actionBundle.putInt(ActivityHome.FRAGMENT_INDEX, ActivityHome.INDEX_FRAGMENT_DEVELOPMENT);
                actionBundle.putInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_TUTORIAL);
                ExecuteIntent.saiyActivity(context, ActivityHome.class, actionBundle, true);
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_FINAL, SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_18), false);
                break;
            case STAGE_FINAL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_FINAL");
                }
                boolean isUserNameUnknown;
                final String currentUserName = SPH.getUserName(context);
                final String defaultUserName = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.master);
                if (UtilsString.notNaked(currentUserName)) {
                    isUserNameUnknown = currentUserName.matches(defaultUserName);
                } else {
                    SPH.setUserName(context, defaultUserName);
                    isUserNameUnknown = true;
                }
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_FINAL: unknownUserName: " + isUserNameUnknown);
                    MyLog.i(CLS_NAME, "action: STAGE_FINAL: errorCount: " + sErrorCount);
                }
                String str;
                if (isUserNameUnknown || sErrorCount > 1) {
                    actionBundle.putInt(ActivityHome.FRAGMENT_INDEX, ActivityHome.INDEX_FRAGMENT_CUSTOMISATION);
                    actionBundle.putInt(LocalRequest.EXTRA_CONDITION, Condition.CONDITION_TUTORIAL);
                    ExecuteIntent.saiyActivity(context, ActivityHome.class, actionBundle, true);
                    str = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_20) + XMLResultsHandler.SEP_SPACE + SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_20b) + XMLResultsHandler.SEP_SPACE + SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_20a);
                } else {
                    str = SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_20) + XMLResultsHandler.SEP_SPACE + SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_20a);
                }
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_RESET, str, false);
                UtilsAnalytic.tutorialComplete(context, FirebaseAnalytics.getInstance(context), sErrorCount > 0);
                break;
            case STAGE_RESET:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_RESET");
                }
                Global.setVoiceTutorialState(context, false);
                break;
            case STAGE_RESET_FOR_ERROR:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "action: STAGE_RESET");
                }
                Global.setVoiceTutorialState(context, false);
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_INVALID, SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_21), false);
                break;
            default:
                Global.setVoiceTutorialState(context, false);
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, STAGE_INVALID, SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.tutorial_22), false);
                break;
        }
    }
}
