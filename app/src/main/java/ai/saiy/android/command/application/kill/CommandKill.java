package ai.saiy.android.command.application.kill;

import android.content.Context;
import android.os.Build;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.nuance.dragon.toolkit.recognition.dictation.parser.XMLResultsHandler;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.algorithms.Algorithm;
import ai.saiy.android.applications.UtilsApplication;
import ai.saiy.android.command.settings.SettingsIntent;
import ai.saiy.android.intent.IntentConstants;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public class CommandKill {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandKill.class.getSimpleName();

    private long then;
    private final Outcome outcome = new Outcome();

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(@NonNull Outcome outcome) {
        if (DEBUG) {
            MyLog.getElapsed(CommandKill.class.getSimpleName(), then);
        }
        return outcome;
    }

    public @NonNull Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest aVar) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !ai.saiy.android.permissions.PermissionHelper.checkUsageStatsPermission(context)) {
            if (ai.saiy.android.intent.ExecuteIntent.settingsIntent(context, IntentConstants.SETTINGS_USAGE_STATS)) {
                outcome.setUtterance(context.getString(R.string.app_speech_usage_stats));
            } else {
                SettingsIntent.settingsIntent(context, SettingsIntent.Type.DEVICE);
                outcome.setUtterance(context.getString(R.string.issue_usage_stats_bug));
            }
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        final ArrayList<String> applicationNames = new ArrayList<>();
        if (aVar.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
            final String applicationName = ((ai.saiy.android.command.settings.application.CommandApplicationSettingsValues) aVar.getVariableData()).getApplicationName();
            if (UtilsString.notNaked(applicationName)) {
                applicationNames.add(applicationName);
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            final ArrayList<String> nameData = new Kill(supportedLanguage).detectKill(context, voiceData);
            if (UtilsList.notNaked(nameData)) {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "nameData: " + nameData.size() + " : " + nameData);
                }
                applicationNames.addAll(nameData);
            }
        }
        if (!UtilsList.notNaked(applicationNames)) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getApplicationUnknownError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        if (DEBUG) {
            MyLog.v(CLS_NAME, "applicationNames size: " + applicationNames.size());
        }
        final ArrayList<Pair<String, String>> runningApplications = UtilsApplication.getRunningApplications(context);
        if (DEBUG) {
            MyLog.v(CLS_NAME, "running apps size: " + runningApplications.size());
        }
        if (!UtilsList.notNaked(runningApplications)) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getApplicationListError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }

        final ArrayList<String> runningApplicationNames = new ArrayList<>(runningApplications.size());
        for (Pair<String, String> stringStringPair : runningApplications) {
            runningApplicationNames.add(stringStringPair.first);
        }
        final ai.saiy.android.nlu.local.AlgorithmicContainer container = new ai.saiy.android.nlu.local.AlgorithmicResolver(context, new Algorithm[]{Algorithm.JARO_WINKLER, Algorithm.SOUNDEX, Algorithm.METAPHONE, Algorithm.DOUBLE_METAPHONE}, supportedLanguage.getLocale(), applicationNames, runningApplicationNames, 500L, false).resolve();
        if (container == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "failed to find a match");
            }
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getRunningApplicationMatchError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "container exactMatch: " + container.isExactMatch());
            MyLog.d(CLS_NAME, "container getInput: " + container.getInput());
            MyLog.d(CLS_NAME, "container getGenericMatch: " + container.getGenericMatch());
            MyLog.d(CLS_NAME, "container getAlgorithm: " + container.getAlgorithm().name());
            MyLog.d(CLS_NAME, "container getScore: " + container.getScore());
            MyLog.d(CLS_NAME, "container getParentPosition: " + container.getParentPosition());
            MyLog.d(CLS_NAME, "container getVariableData: " + container.getVariableData());
        }
        Pair<String, String> applicationPair = null;
        try {
            applicationPair = runningApplications.get(container.getParentPosition());
        } catch (IndexOutOfBoundsException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "applicationList IndexOutOfBoundsException");
                e.printStackTrace();
            }
        }
        if (applicationPair == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "index out of bounds");
            }
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getRunningApplicationMatchError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(outcome);
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "applicationPair name: " + applicationPair.first);
            MyLog.d(CLS_NAME, "applicationPair package: " + applicationPair.second);
        }
        ai.saiy.android.intent.ExecuteIntent.goHome(context);
        if (UtilsApplication.killPackage(context, applicationPair.second)) {
            outcome.setUtterance(context.getString(R.string.killed) + XMLResultsHandler.SEP_SPACE + applicationPair.first);
            outcome.setOutcome(Outcome.SUCCESS);
            return returnOutcome(outcome);
        }
        outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getKillApplicationError(context, supportedLanguage, applicationPair.first));
        outcome.setOutcome(Outcome.FAILURE);
        return returnOutcome(outcome);
    }
}
