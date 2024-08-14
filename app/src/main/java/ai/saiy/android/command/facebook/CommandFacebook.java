package ai.saiy.android.command.facebook;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.facebook.share.model.ShareLinkContent;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Condition;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.ui.activity.ActivityFacebook;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class CommandFacebook {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandFacebook.class.getSimpleName();

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

    public @NonNull Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        this.then = System.nanoTime();
        final Outcome outcome = new Outcome();
        outcome.setAction(LocalRequest.ACTION_SPEAK_LISTEN);
        outcome.setOutcome(Outcome.SUCCESS);
        outcome.setCondition(Condition.CONDITION_NONE);
        if (new com.facebook.share.ShareApi(new ShareLinkContent.Builder().build()).canShare()) {
            final CommandFacebookValues commandFacebookValues = new Facebook(supportedLanguage).sort(context, voiceData);
            if (commandFacebookValues.isResolved() && UtilsString.notNaked(commandFacebookValues.getText())) {
                outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getFacebookConfirmation(context, supportedLanguage, commandFacebookValues.getText()));
            } else {
                outcome.setUtterance(ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.facebook_content_request));
            }
            outcome.setCondition(Condition.CONDITION_FACEBOOK);
            commandFacebookValues.setContentType(FacebookConfirm.ContentType.FACEBOOK_CONTENT);
            outcome.setExtra(commandFacebookValues);
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "canShare false");
            }
            outcome.setUtterance(ai.saiy.android.localisation.SaiyResourcesHelper.getStringResource(context, supportedLanguage, R.string.facebook_auth_request));
            outcome.setAction(LocalRequest.ACTION_SPEAK_ONLY);
            final Bundle bundle = new Bundle();
            bundle.putInt(ActivityFacebook.EXTRA_REQUEST_TYPE, ActivityFacebook.TYPE_AUTH);
            ai.saiy.android.intent.ExecuteIntent.saiyActivity(context, ActivityFacebook.class, bundle, true);
        }
        return returnOutcome(outcome);
    }
}
