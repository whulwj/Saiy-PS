package ai.saiy.android.command.location.helper;

import android.Manifest;
import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.PermissionChecker;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.ExecutionException;

import ai.saiy.android.utils.MyLog;

/**
 * Native location helper of google location API
 */
public class FusedLocationHelper implements ResultCallback<Status> {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FusedLocationHelper.class.getSimpleName();

    private Context mContext;
    private FusedLocationProviderClient mFusedLocationClient;

    public void prepare(Context context) {
        this.mContext = context;
        final GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        final int connectionResult = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (connectionResult == ConnectionResult.SUCCESS) {
            this.mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "prepare: play services unavailable");
            }
            googleApiAvailability.showErrorNotification(context, connectionResult);
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onResult");
        }
    }

    public @Nullable Location getLastLocation() {
        if (mFusedLocationClient == null) {
            return null;
        }
        if (PermissionChecker.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PermissionChecker.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PermissionChecker.PERMISSION_GRANTED) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "no location permission");
            }
            return null;
        }
        final Task<Location> task = mFusedLocationClient.getLastLocation();
        try {
            // Block on a task and get the result synchronously. This is generally done
            // when executing a task inside a separately managed background thread. Doing this
            // on the main (UI) thread can cause your application to become unresponsive.
            final Location lastLocation = Tasks.await(task);
            if (lastLocation != null) {
                return lastLocation;
            } else if (DEBUG) {
                MyLog.d(CLS_NAME, "no location");
            }
        } catch (ExecutionException e) {
            // The Task failed, this is the same exception you'd get in a non-blocking failure handler.
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getLastLocation:" + e.getClass().getSimpleName() + ", " + e.getMessage());
            }
        } catch (InterruptedException e) {
            // An interrupt occurred while waiting for the task to complete.
            if (DEBUG) {
                MyLog.w(CLS_NAME, "getLastLocation:" + e.getClass().getSimpleName() + ", " + e.getMessage());
            }
        }
        return null;
    }

    public void destroy() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient = null;
            if (DEBUG) {
                MyLog.i(CLS_NAME, "destroy");
            }
        }
    }
}
