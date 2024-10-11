package ai.saiy.android.command.calculate;

import android.content.Context;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.processing.Position;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;

public final class CommandCalculate {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandCalculate.class.getSimpleName();

    private long then;

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }

    public @NonNull Outcome getResponse(@NonNull Context context, @NonNull ArrayList<String> voiceData, @NonNull SupportedLanguage supportedLanguage, @NonNull ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        ArrayList<String> calculationData = null;
        final Outcome outcome = new Outcome();
        if (!cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            calculationData = new Calculate(supportedLanguage).sortCalculation(context, voiceData);
            if (DEBUG) {
                MyLog.d(CLS_NAME, "calculationData: " + calculationData.size() + " : " + calculationData);
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "isResolved: true");
        }
        if (!UtilsList.notNaked(calculationData)) {
            outcome.setUtterance(PersonalityResponse.getCalculateUnknownError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }

        final MathEval mathEval = new MathEval();
        for (String calculation : calculationData) {
            if (calculation.replaceAll(MathEval.SQUARE_ROOT, "").matches("[^a-z]+")) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "contains[^a-z]+: true");
                }
                try {
                    double answer = mathEval.evaluate(calculation);
                    if (DEBUG) {
                        MyLog.d(CLS_NAME, "answer: " + answer);
                    }
                    final ai.saiy.android.processing.Qubit qubit = new ai.saiy.android.processing.Qubit();
                    qubit.setClipboardContent(String.valueOf(answer));
                    outcome.setQubit(qubit);
                    final ai.saiy.android.processing.EntangledPair entangledPair = new ai.saiy.android.processing.EntangledPair(Position.TOAST_LONG, CC.COMMAND_CALCULATE);
                    entangledPair.setToastContent(calculation + " = " + answer);
                    outcome.setEntangledPair(entangledPair);
                    outcome.setOutcome(Outcome.SUCCESS);
                    final ai.saiy.android.localisation.SaiyResources sr = new ai.saiy.android.localisation.SaiyResources(context, supportedLanguage);
                    final String multiplied_by = sr.getString(R.string._MULTIPLIED_BY_);
                    final String divided_by = sr.getString(R.string._DIVIDED_BY_);
                    final String equals = sr.getString(R.string.EQUALS);
                    sr.reset();
                    String utterance = calculation.replaceAll("\\*", multiplied_by).replaceAll("/", divided_by) + ". " + equals + XMLResultsHandler.SEP_SPACE + answer;
                    if (ai.saiy.android.utils.SPH.getCalculateCommandVerbose(context) >= 3) {
                        outcome.setUtterance(utterance);
                    } else {
                        ai.saiy.android.utils.SPH.incrementCalculateCommandVerbose(context);
                        outcome.setUtterance(utterance + ". " + PersonalityResponse.getClipboardSpell(context, supportedLanguage));
                    }
                    return returnOutcome(outcome);
                } catch (ArithmeticException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "ArithmeticException");
                        e.printStackTrace();
                    }
                } catch (NumberFormatException e) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "NumberFormatException");
                        e.printStackTrace();
                    }
                }
            } else if (DEBUG) {
                MyLog.v(CLS_NAME, "Skipping simple mathEval: " + calculation);
            }
        }

        if (ai.saiy.android.applications.Installed.isPackageInstalled(context, ai.saiy.android.applications.Installed.PACKAGE_WOLFRAM_ALPHA)) {
            ai.saiy.android.intent.ExecuteIntent.wolframAlpha(context, calculationData.get(0));
        } else {
            ai.saiy.android.intent.ExecuteIntent.webSearch(context, "https://www.wolframalpha.com/input/?i=" + calculationData.get(0).replaceAll("\\s", "%20"));
        }
        outcome.setUtterance(PersonalityResponse.getCalculateWolframAlpha(context, supportedLanguage));
        outcome.setOutcome(Outcome.FAILURE);
        return returnOutcome(outcome);
    }
}
