package ai.saiy.android.command.location.vehicle.locate;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import ai.saiy.android.R;
import ai.saiy.android.command.navigation.UtilsNavigation;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.MyLog;

public class CommandLocateVehicle {
    private static final boolean DEBUG = MyLog.DEBUG;

    public @NonNull Outcome getResponse(Context context, SupportedLanguage supportedLanguage) {
        final long then = System.nanoTime();
        final Outcome outcome = new Outcome();
        final Pair<Double, Double> locationPair = ai.saiy.android.utils.SPH.getLastLocation(context);
        if (locationPair == null) {
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getLocationUnknownError(context, supportedLanguage));
            outcome.setOutcome(Outcome.FAILURE);
        } else if (UtilsNavigation.navigateToAddress(context, locationPair.first, locationPair.second, "&mode=w")) {
            outcome.setOutcome(Outcome.SUCCESS);
            outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getLocateVehicle(context, supportedLanguage));
        } else {
            outcome.setOutcome(Outcome.FAILURE);
            outcome.setUtterance(context.getString(R.string.error_navigation_app_failed));
        }
        if (DEBUG) {
            MyLog.getElapsed(CommandLocateVehicle.class.getSimpleName(), then);
        }
        return outcome;
    }
}
