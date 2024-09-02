package ai.saiy.android.command.location.vehicle.parked;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;

import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.command.location.LocationHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

public class CommandParkedVehicle {
    private static final boolean DEBUG = MyLog.DEBUG;

    public @NonNull Outcome getResponse(Context context, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        final long then = System.nanoTime();
        final Outcome outcome = new Outcome();
        if (ai.saiy.android.permissions.PermissionHelper.checkLocationPermissions(context, cr.getBundle())) {
            Location location;
            if (SPH.getLocationProvider(context) == Constants.DEFAULT_LOCATION_PROVIDER) {
                final LocationHelper locationHelper = new LocationHelper();
                location = locationHelper.getLastKnownLocation(context);
            } else {
                final ai.saiy.android.command.location.helper.FusedLocationHelper fusedLocationHelper = new ai.saiy.android.command.location.helper.FusedLocationHelper();
                fusedLocationHelper.prepare(context);
                location = fusedLocationHelper.getLastLocation();
                fusedLocationHelper.destroy();
            }
            if (location != null) {
                ai.saiy.android.utils.SPH.setLocation(context, location);
                outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getParkedVehicle(context, supportedLanguage));
                outcome.setOutcome(Outcome.SUCCESS);
            } else {
                outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getLocationAccessError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
            }
        } else {
            outcome.setUtterance(SaiyRequestParams.SILENCE);
            outcome.setOutcome(Outcome.FAILURE);
        }
        if (DEBUG) {
            MyLog.getElapsed(CommandParkedVehicle.class.getSimpleName(), then);
        }
        return outcome;
    }
}
