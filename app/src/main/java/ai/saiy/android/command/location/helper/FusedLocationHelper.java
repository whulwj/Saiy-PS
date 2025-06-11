package ai.saiy.android.command.location.helper;

import android.Manifest;
import android.content.Context;

import androidx.annotation.Nullable;
import androidx.core.content.PermissionChecker;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.concurrent.TimeUnit;

import ai.saiy.android.command.location.LocationHelper;
import ai.saiy.android.location.GoogleLocationService;
import ai.saiy.android.location.LocalLocation;
import ai.saiy.android.location.LocationService;
import ai.saiy.android.utils.MyLog;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

/**
 * Native location helper of google location API
 */
public class FusedLocationHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FusedLocationHelper.class.getSimpleName();

    private Context mContext;
    private LocationService mLocationService;

    public void prepare(Context context) {
        this.mContext = context;
        final GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        final int connectionResult = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (connectionResult == ConnectionResult.SUCCESS) {
            this.mLocationService = new GoogleLocationService(context);
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "prepare: play services unavailable");
            }
            try {
                googleApiAvailability.showErrorNotification(context, connectionResult);
            } catch (Throwable t) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "prepare: " + t.getClass().getSimpleName() + "," + t.getMessage());
                }
            }
        }
    }

    public @Nullable LocalLocation getLocation() {
        if (mLocationService == null) {
            return null;
        }
        if (PermissionChecker.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PermissionChecker.PERMISSION_GRANTED &&
                PermissionChecker.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PermissionChecker.PERMISSION_GRANTED) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "no location permission");
            }
            return null;
        }
        return mLocationService.getCurrentLocation(true, LocationHelper.isLocationEnabled(mContext)? LocationService.LOCATION_REQUEST_TIME_OUT:LocationService.LOCAL_LOCATION_REQUEST_TIME_OUT, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .onErrorComplete(throwable -> {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onError: " + throwable.getClass().getSimpleName() + ", " + throwable.getMessage());
                    }
                    return true;
                }).blockingGet();
    }

    public void destroy() {
        if (mLocationService != null) {
            mLocationService = null;
            if (DEBUG) {
                MyLog.i(CLS_NAME, "destroy");
            }
        }
    }
}
