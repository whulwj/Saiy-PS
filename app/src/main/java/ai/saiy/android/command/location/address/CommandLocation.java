package ai.saiy.android.command.location.address;

import android.content.Context;
import android.location.Location;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import ai.saiy.android.api.request.SaiyRequestParams;
import ai.saiy.android.localisation.SupportedLanguage;
import ai.saiy.android.processing.Outcome;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

public class CommandLocation {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = CommandLocation.class.getSimpleName();

    public @NonNull Outcome getResponse(Context context, SupportedLanguage supportedLanguage, ai.saiy.android.command.helper.CommandRequest cr) {
        final long then = System.nanoTime();
        final Outcome outcome = new Outcome();
        if (ai.saiy.android.permissions.PermissionHelper.checkLocationPermissions(context, cr.getBundle())) {
            final ai.saiy.android.command.location.LocationHelper locationHelper = new ai.saiy.android.command.location.LocationHelper();
            Location location;
            if (SPH.getLocationProvider(context) == Constants.DEFAULT_LOCATION_PROVIDER || GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
                location = locationHelper.getLastKnownLocation(context);
            } else {
                final ai.saiy.android.command.location.helper.FusedLocationHelper fusedLocationHelper = new ai.saiy.android.command.location.helper.FusedLocationHelper();
                fusedLocationHelper.prepare(context);
                location = fusedLocationHelper.getLastLocation();
                fusedLocationHelper.destroy();
            }
            if (location != null) {
                final Pair<Boolean, String> addressPair = locationHelper.getAddress(context, supportedLanguage, location);
                if (addressPair.first && UtilsString.notNaked(addressPair.second)) {
                    outcome.setUtterance(addressPair.second);
                    outcome.setOutcome(Outcome.SUCCESS);
                } else {
                    outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getAddressUnknownError(context, supportedLanguage));
                    outcome.setOutcome(Outcome.FAILURE);
                }
            } else {
                outcome.setUtterance(ai.saiy.android.personality.PersonalityResponse.getLocationAccessError(context, supportedLanguage));
                outcome.setOutcome(Outcome.FAILURE);
            }
        } else {
            outcome.setUtterance(SaiyRequestParams.SILENCE);
            outcome.setOutcome(Outcome.FAILURE);
        }
        if (DEBUG) {
            MyLog.getElapsed(CLS_NAME, then);
        }
        return outcome;
    }
}
