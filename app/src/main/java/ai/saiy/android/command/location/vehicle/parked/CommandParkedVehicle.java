package ai.saiy.android.command.location.vehicle.parked;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.command.location.LocationHelper;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.location.LocalLocation;
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
            LocalLocation location;
            if (SPH.getLocationProvider(context) == Constants.DEFAULT_LOCATION_PROVIDER || GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
                final LocationHelper locationHelper = new LocationHelper();
                location = locationHelper.getLocation(context);
            } else {
                final ai.saiy.android.command.location.helper.FusedLocationHelper fusedLocationHelper = new ai.saiy.android.command.location.helper.FusedLocationHelper();
                fusedLocationHelper.prepare(context);
                location = fusedLocationHelper.getLocation();
                fusedLocationHelper.destroy();
            }
            if (location != null && !location.isFake()) {
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
