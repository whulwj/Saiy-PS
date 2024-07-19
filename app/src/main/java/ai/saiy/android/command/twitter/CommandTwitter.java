package ai.saiy.android.command.twitter;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.command.search.provider.TwitterHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityTwitterOAuth;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class CommandTwitter {
    private final boolean DEBUG = ai.saiy.android.utils.MyLog.DEBUG;
    private final String CLS_NAME = CommandTwitter.class.getSimpleName();

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

    public Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
        outcome.setOutcome(Outcome.SUCCESS);
        outcome.setCondition(Condition.CONDITION_NONE);
        final String token = ai.saiy.android.utils.SPH.getTwitterToken(context);
        final String tokenSecret = ai.saiy.android.utils.SPH.getTwitterSecret(context);
        if (!UtilsString.notNaked(token) || !UtilsString.notNaked(tokenSecret)) {
            if (DEBUG) {
                ai.saiy.android.utils.MyLog.w(CLS_NAME, "token or secret naked");
            }
            outcome.setUtterance(ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.twitter_auth_request));
            outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
            ai.saiy.android.intent.ExecuteIntent.saiyActivity(context, ActivityTwitterOAuth.class, null, true);
        } else if (TwitterHelper.isCredentialsValid(context)) {
            final CommandTwitterValues commandTwitterValues = new Twitter(supportedLanguage).sort(context, voiceData);
            outcome.setCondition(Condition.CONDITION_TWITTER);
            if (commandTwitterValues.isResolved() && UtilsString.notNaked(commandTwitterValues.getText())) {
                outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getTwitterConfirmation(context, supportedLanguage, commandTwitterValues.getText()));
                commandTwitterValues.setContentType(TwitterConfirm.ContentType.TWITTER);
            } else {
                outcome.setUtterance(ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.twitter_content_request));
                commandTwitterValues.setContentType(TwitterConfirm.ContentType.TWITTER_CONTENT);
            }
            outcome.setExtra(commandTwitterValues);
        } else {
            if (DEBUG) {
                ai.saiy.android.utils.MyLog.w(CLS_NAME, "credentials invalid");
            }
            ai.saiy.android.utils.SPH.setTwitterSecret(context, null);
            ai.saiy.android.utils.SPH.setTwitterToken(context, null);
            outcome.setUtterance(ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.twitter_reauth_request));
            outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
            ai.saiy.android.intent.ExecuteIntent.saiyActivity(context, ActivityTwitterOAuth.class, null, true);
        }
        return returnOutcome(outcome);
    }
}
