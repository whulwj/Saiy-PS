package ai.saiy.android.command.web;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;

public final class CommandWeb {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandWeb.class.getSimpleName();

    private long then;

    /**
     * A single point of return to check the elapsed time for debugging.
     *
     * @param outcome the constructed {@link Outcome}
     * @return the constructed {@link Outcome}
     */
    private Outcome returnOutcome(@NonNull Outcome outcome) {
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
        final Outcome outcome = new Outcome();
        outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
        List<String> webUrls = Collections.emptyList();
        if (!cr.isResolved()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "isResolved: false");
            }
            webUrls = new Web(supportedLanguage).getUrls(context, voiceData);
            if (DEBUG) {
                for (String webString : webUrls) {
                    MyLog.i(CLS_NAME, "webString: " + webString);
                }
            }
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "isResolved: true");
        }
        if (UtilsList.notNaked(webUrls)) {
            for (String webString : webUrls) {
                if (ai.saiy.android.utils.conditions.Network.isWebAddressReachable(context, webString) && ai.saiy.android.intent.ExecuteIntent.webSearch(context, webString)) {
                    outcome.setOutcome(Outcome.SUCCESS);
                    outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getGenericAcknowledgement(context, supportedLanguage));
                    return returnOutcome(outcome);
                }
            }
        }

        final String searchTerm = new Web(supportedLanguage).getSearchTerm(context, voiceData);
        if (UtilsString.notNaked(searchTerm)) {
            ai.saiy.android.intent.ExecuteIntent.googleNowOrGoogleWeb(context, supportedLanguage, searchTerm, null);
        }
        outcome.setOutcome(Outcome.FAILURE);
        if (!UtilsList.notNaked(webUrls)) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getWebAddressFormatError(context, supportedLanguage));
        } else {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getWebAddressUnreachableError(context, supportedLanguage));
        }
        return returnOutcome(outcome);
    }
}
