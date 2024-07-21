package ai.saiy.android.command.call;

import android.content.Context;

import java.util.ArrayList;

import ai.saiy.android.R;
import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.personality.PersonalityResponse;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsString;

public class CommandCallBack {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandCallBack.class.getSimpleName();

    public Outcome getResponse(Context context, ArrayList<String> voiceData, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "voiceData: " + voiceData.size() + " : " + voiceData);
        }
        final long then = System.nanoTime();
        final Outcome outcome = new Outcome();
        if (!CallHelper.hasTelephonyFeature(context)) {
            outcome.setOutcome(Outcome.SUCCESS);
            outcome.setUtterance(PersonalityResponse.getHardwareUnsupportedError(context, supportedLanguage));
        } else if (ai.saiy.android.permissions.PermissionHelper.checkTelephonyGroupPermissions(context, cr.getBundle())) {
            final String lastMissedCall = CallHelper.getLastMissedCall(context);
            if (UtilsString.notNaked(lastMissedCall)) {
                ai.saiy.android.utils.SPH.setLastContactUpdate(context, System.currentTimeMillis() - 170000);
                if (CallHelper.callNumber(context, lastMissedCall)) {
                    outcome.setOutcome(Outcome.SUCCESS);
                    outcome.setUtterance(SaiyRequestParams.SILENCE);
                } else {
                    outcome.setOutcome(Outcome.FAILURE);
                    outcome.setUtterance(PersonalityResponse.getCallingNumberError(context, supportedLanguage, context.getString(R.string.calling)));
                    CallHelper.dialNumber(context, lastMissedCall);
                }
            } else {
                outcome.setOutcome(Outcome.FAILURE);
                outcome.setUtterance(PersonalityResponse.getMissedCallError(context, supportedLanguage));
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
