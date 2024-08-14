package ai.saiy.android.command.call;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class CommandRedial {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandRedial.class.getSimpleName();

    public @NonNull Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        final long then = System.nanoTime();
        final Outcome outcome = new Outcome();
        if (!CallHelper.hasTelephonyFeature(context)) {
            outcome.setOutcome(Outcome.SUCCESS);
            outcome.setUtterance(PersonalityResponse.getHardwareUnsupportedError(context, supportedLanguage));
        } else if (ai.saiy.android.permissions.PermissionHelper.checkTelephonyGroupPermissions(context, cr.getBundle())) {
            final String lastOutgoingCall = CallHelper.getLastOutgoingCall(context);
            if (UtilsString.notNaked(lastOutgoingCall)) {
                ai.saiy.android.utils.SPH.setLastContactUpdate(context, System.currentTimeMillis() - 170000);
                if (CallHelper.callNumber(context, lastOutgoingCall)) {
                    outcome.setOutcome(Outcome.SUCCESS);
                    outcome.setUtterance(SaiyRequestParams.SILENCE);
                } else {
                    outcome.setOutcome(Outcome.FAILURE);
                    outcome.setUtterance(PersonalityResponse.getCallingNumberError(context, supportedLanguage, context.getString(R.string.calling)));
                    CallHelper.dialNumber(context, lastOutgoingCall);
                }
            } else {
                outcome.setOutcome(Outcome.FAILURE);
                outcome.setUtterance(PersonalityResponse.getRedialError(context, supportedLanguage));
            }
        } else {
            outcome.setOutcome(Outcome.SUCCESS);
            outcome.setUtterance(SaiyRequestParams.SILENCE);
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }
}
