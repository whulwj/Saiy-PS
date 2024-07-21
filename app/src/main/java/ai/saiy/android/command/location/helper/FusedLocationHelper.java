package ai.saiy.android.command.location.helper;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationServices;

import ai.saiy.android.utils.MyLog;

public class FusedLocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FusedLocationHelper.class.getSimpleName();

    private GoogleApiClient googleApiClient;

    public void connect() {
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onConnectionSuspended");
        }
    }

    public void prepare(Context context) {
        final GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        final int playServicesAvailable = googleApiAvailability.isGooglePlayServicesAvailable(context);
        if (playServicesAvailable == ConnectionResult.SUCCESS) {
            this.googleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "prepare: play services unavailable");
            }
            googleApiAvailability.showErrorNotification(context, playServicesAvailable);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onConnected");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onConnectionFailed");
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onResult");
        }
    }

    public @Nullable Location getLastLocation() {
        if (googleApiClient == null) {
            return null;
        }
        if (googleApiClient.isConnecting()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "apiClient.isConnecting");
            }
            try {
                Thread.sleep(1500L);
            } catch (InterruptedException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "InterruptedException");
                }
            }
        }
        try {
            return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } catch (SecurityException e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "SecurityException");
                e.printStackTrace();
            }
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }
        return null;
    }

    public void destroy() {
        if (googleApiClient != null) {
            try {
                googleApiClient.disconnect();
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "destroy: Exception");
                    e.printStackTrace();
                }
            }
        }
    }
}
