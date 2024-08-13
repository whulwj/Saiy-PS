package ai.saiy.android.command.settings.application;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.algorithms.Algorithm;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public class CommandApplicationSettings {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandApplicationSettings.class.getSimpleName();

    private long then;
    private final Outcome outcome = new Outcome();

    /**
     * A single point of return to check the elapsed time in debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @param deleteList true if need to delete the local application list
     * @return the constructed {@link Outcome}
     */
    private @NonNull Outcome returnOutcome(Context context, @NonNull Outcome outcome, boolean deleteList) {
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        if (deleteList) {
            new ai.saiy.android.database.helper.DatabaseHelper().deleteApplications(context);
        }
        return outcome;
    }

    public Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final ArrayList<String> applicationNames = new ArrayList<>();
        if (cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: true");
            }
            final String applicationName = ((CommandApplicationSettingsValues) cr.getVariableData()).getApplicationName();
            if (UtilsString.notNaked(applicationName)) {
                applicationNames.add(applicationName);
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            final ArrayList<String> nameData = new ApplicationSettings(supportedLanguage).detectApplicationNames(context, voiceData);
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
            return returnOutcome(context, outcome, false);
        }
        if (DEBUG) {
            MyLog.v(CLS_NAME, "applicationNames size: " + applicationNames.size());
        }

        final ArrayList<Pair<String, String>> applicationList = ai.saiy.android.applications.Installed.getInstalledApplications(context);
        if (!UtilsList.notNaked(applicationList)) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getApplicationListError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(context, outcome, true);
        }
        final ArrayList<String> applicationLabelList = new ArrayList<>(applicationList.size());
        for (Pair<String, String> stringPair : applicationList) {
            applicationLabelList.add(stringPair.first);
        }
        final ai.saiy.android.nlu.local.AlgorithmicContainer algorithmicContainer = new ai.saiy.android.nlu.local.AlgorithmicResolver(context, new Algorithm[]{Algorithm.JARO_WINKLER, Algorithm.SOUNDEX, Algorithm.METAPHONE, Algorithm.DOUBLE_METAPHONE}, supportedLanguage.getLocale(), applicationNames, applicationLabelList, 500L, false).resolve();
        if (algorithmicContainer == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "failed to find a match");
            }
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getApplicationMatchError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(context, outcome, true);
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "container exactMatch: " + algorithmicContainer.isExactMatch());
            MyLog.d(CLS_NAME, "container getInput: " + algorithmicContainer.getInput());
            MyLog.d(CLS_NAME, "container getGenericMatch: " + algorithmicContainer.getGenericMatch());
            MyLog.d(CLS_NAME, "container getAlgorithm: " + algorithmicContainer.getAlgorithm().name());
            MyLog.d(CLS_NAME, "container getScore: " + algorithmicContainer.getScore());
            MyLog.d(CLS_NAME, "container getParentPosition: " + algorithmicContainer.getParentPosition());
            MyLog.d(CLS_NAME, "container getVariableData: " + algorithmicContainer.getVariableData());
        }
        Pair<String, String> applicationPair = null;
        try {
            applicationPair = applicationList.get(algorithmicContainer.getParentPosition());
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
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getApplicationMatchError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
            return returnOutcome(context, outcome, true);
        }
        if (DEBUG) {
            MyLog.d(CLS_NAME, "applicationPair name: " + applicationPair.first);
            MyLog.d(CLS_NAME, "applicationPair package: " + applicationPair.second);
        }
        if (ai.saiy.android.applications.UtilsApplication.openApplicationSpecificSettings(context, applicationPair.second)) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getDisplayedApplicationSettings(context, supportedLanguage));
            outcome.setOutcome(Outcome.SUCCESS);
            return returnOutcome(context, outcome, false);
        }
        outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getSettingsDisplayError(context, supportedLanguage));
        outcome.setOutcome(Outcome.FAILURE);
        return returnOutcome(context, outcome, true);
    }
}
