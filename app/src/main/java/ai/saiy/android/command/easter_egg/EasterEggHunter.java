package ai.saiy.android.command.easter_egg;

import android.content.Context;
import android.os.Bundle;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.Locale;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

public final class EasterEggHunter {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = EasterEggHunter.class.getSimpleName();

    private final Context context;
    private final Locale vrLocale;
    private final Locale ttsLocale;
    private final SupportedLanguage sl;
    private final EasterEggLocal easterEggLocal;

    public EasterEggHunter(@NonNull Context context, @NonNull Locale vrLocale, @NonNull Locale ttsLocale, @NonNull SupportedLanguage supportedLanguage, @NonNull Bundle bundle) {
        this.context = context.getApplicationContext();
        this.vrLocale = vrLocale;
        this.ttsLocale = ttsLocale;
        this.sl = supportedLanguage;
        this.easterEggLocal = new EasterEggLocal(sl, bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
    }

    private void executeRequest(int action, String utterance, boolean isEasterEggRequest) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "executeRequest");
        }
        final ai.saiy.android.service.helper.LocalRequest localRequest = new ai.saiy.android.service.helper.LocalRequest(context);
        localRequest.prepareDefault(action, sl, vrLocale, ttsLocale, utterance);
        if (isEasterEggRequest) {
            localRequest.setCondition(Condition.CONDITION_EASTER_EGG);
        }
        localRequest.execute();
    }

    public void hunt() {
        String utterance;
        int action = LocalRequest.ACTION_SPEAK_LISTEN;
        if (DEBUG) {
            MyLog.i(CLS_NAME, "hunt");
        }
        final int currentState = SPH.getEasterEggState(context);
        switch (currentState) {
            case EasterEggHunter.STAGE_1:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "hunt: STAGE_1");
                }
                boolean isSageOneFound = easterEggLocal.stageOneFound();
                if (isSageOneFound) {
                    SPH.setEasterEggState(context, EasterEggHunter.STAGE_2);
                    utterance = EasterEggLocal.stageOneAnswer() + XMLResultsHandler.SEP_SPACE + "[wand] " + XMLResultsHandler.SEP_SPACE + EasterEggLocal.getEasterEggStage(context, sl, EasterEggHunter.STAGE_2);
                } else {
                    utterance = EasterEggLocal.stageOneIncorrect(context, sl);
                    action = LocalRequest.ACTION_SPEAK_ONLY;
                }
                executeRequest(action, utterance, isSageOneFound);
                break;
            case EasterEggHunter.STAGE_2:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "hunt: STAGE_2");
                }
                boolean isSageTwoFound = easterEggLocal.stageTwoFound();
                if (isSageTwoFound) {
                    SPH.setEasterEggState(context, EasterEggHunter.STAGE_3);
                    utterance = EasterEggLocal.stageTwoAnswer() + XMLResultsHandler.SEP_SPACE + "[wand] " + XMLResultsHandler.SEP_SPACE + EasterEggLocal.getEasterEggStage(context, sl, EasterEggHunter.STAGE_3);
                } else {
                    utterance = EasterEggLocal.stageTwoIncorrect(context, sl);
                    action = LocalRequest.ACTION_SPEAK_ONLY;
                }
                executeRequest(action, utterance, isSageTwoFound);
                break;
            case EasterEggHunter.STAGE_3:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "hunt: STAGE_3");
                }
                boolean isSageThreeFound = easterEggLocal.stageThreeFound();
                if (isSageThreeFound) {
                    SPH.setEasterEggState(context, EasterEggHunter.STAGE_4);
                    utterance = EasterEggLocal.stageThreeAnswer() + XMLResultsHandler.SEP_SPACE + "[wand] " + XMLResultsHandler.SEP_SPACE + EasterEggLocal.getEasterEggStage(context, sl, EasterEggHunter.STAGE_4);
                } else {
                    utterance = EasterEggLocal.stageThreeIncorrect(context, sl);
                }
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, utterance, isSageThreeFound);
                break;
            case EasterEggHunter.STAGE_4:
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "hunt: STAGE_4");
                }
                break;
            case EasterEggHunter.STAGE_5:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "hunt: STAGE_5");
                }
                SPH.setEasterEggState(context, EasterEggHunter.STAGE_6);
                executeRequest(LocalRequest.ACTION_SPEAK_LISTEN, EasterEggLocal.stageFiveAnswer() + XMLResultsHandler.SEP_SPACE + "[wand] " + XMLResultsHandler.SEP_SPACE + EasterEggLocal.getEasterEggStage(context, sl, EasterEggHunter.STAGE_6), true);
                break;
            case EasterEggHunter.STAGE_6:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "hunt: STAGE_6");
                }
                if (easterEggLocal.stageSixFound()) {
                    SPH.setEasterEggState(context, EasterEggHunter.STAGE_7);
                    utterance = EasterEggLocal.stageSixAnswer() + XMLResultsHandler.SEP_SPACE + "[wand] " + XMLResultsHandler.SEP_SPACE + EasterEggLocal.getEasterEggStage(context, sl, EasterEggHunter.STAGE_7);
                } else {
                    utterance = EasterEggLocal.stageSixIncorrect();
                }
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, utterance, false);
                break;
            case EasterEggHunter.STAGE_7:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "hunt: STAGE_7");
                }
                executeRequest(LocalRequest.ACTION_SPEAK_ONLY, EasterEggLocal.getEasterEggStage(context, sl, EasterEggHunter.STAGE_7), false);
                break;
            default:
                break;
        }
    }

    public static final int STAGE_1 = 1;
    public static final int STAGE_2 = 2;
    public static final int STAGE_3 = 3;
    public static final int STAGE_4 = 4;
    public static final int STAGE_5 = 5;
    public static final int STAGE_6 = 6;
    public static final int STAGE_7 = 7;
}
