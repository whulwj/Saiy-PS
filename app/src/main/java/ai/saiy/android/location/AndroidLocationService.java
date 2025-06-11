package ai.saiy.android.location;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.content.PermissionChecker;
import androidx.core.location.LocationManagerCompat;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.utils.MyLog;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Native Location Service of Android framework API
 */
public final class AndroidLocationService implements LocationService {
    private final Context mContext;
    private final RxLocationListener mLocationListener;
    private final LocationManager mLocationManager;
    private boolean mIsLocationByGpsRegistered, mIsLocationByNetworkRegistered, mIsLocationByFusedRegistered;

    public AndroidLocationService(Context context) {
        this.mContext = context;
        this.mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.mLocationListener = new RxLocationListener(context);
    }

    @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
    private void requestLocation() {
        final boolean fineLocationGranted = (PermissionChecker.PERMISSION_GRANTED == PermissionChecker.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION));
        final boolean hasGps = fineLocationGranted && LocationManagerCompat.hasProvider(mLocationManager, LocationManager.GPS_PROVIDER) && mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        final boolean hasNetwork = LocationManagerCompat.hasProvider(mLocationManager, LocationManager.NETWORK_PROVIDER) && mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        final boolean hasFused = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) && fineLocationGranted && LocationManagerCompat.hasProvider(mLocationManager, LocationManager.FUSED_PROVIDER);

        if (hasGps && !mIsLocationByGpsRegistered) {
            requestSingleUpdate(LocationManager.GPS_PROVIDER);
            mIsLocationByGpsRegistered = true;
        }

        if (hasNetwork && !mIsLocationByNetworkRegistered) {
            requestSingleUpdate(LocationManager.NETWORK_PROVIDER);
            mIsLocationByNetworkRegistered = true;
        }
        if (hasFused && !mIsLocationByFusedRegistered) {
            requestSingleUpdate(LocationManager.FUSED_PROVIDER);
            mIsLocationByFusedRegistered = true;
        }
        if (!hasGps && !hasNetwork && !hasFused && DEBUG) {
            MyLog.w(CLS_NAME, "requestLocation");
        }
    }

    @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
    private void requestSingleUpdate(@NonNull String provider) {
        mLocationManager.requestSingleUpdate(provider, mLocationListener, null);
    }

    private @Nullable LocalLocation getLastKnownLocation() {
        final ArrayList<Location> pendingLocationList = new ArrayList<>(5);
        final boolean fineLocationGranted = (PermissionChecker.PERMISSION_GRANTED == PermissionChecker.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION));
        if (fineLocationGranted) {
            final Location lastKnownLocationByGps = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocationByGps != null) {
                pendingLocationList.add(lastKnownLocationByGps);
            }
        }
        final Location lastKnownLocationByNetwork = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastKnownLocationByNetwork != null) {
            pendingLocationList.add(lastKnownLocationByNetwork);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && fineLocationGranted) {
            final Location lastKnownLocationByFused = mLocationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER);
            if (lastKnownLocationByFused != null) {
                pendingLocationList.add(lastKnownLocationByFused);
            }
        }
        if (fineLocationGranted) {
            final Location lastKnownLocationByPassive = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (lastKnownLocationByPassive != null) {
                pendingLocationList.add(lastKnownLocationByPassive);
            }
        }

        Location bestLocation = null;
        Location location;
        for (int i = pendingLocationList.size() - 1; i >= 0; --i) {
            location = pendingLocationList.get(i);
            if (UtilsLocatio.isBetterLocation(location, bestLocation)) {
                // Found best last known location
                bestLocation = location;
            }
        }

        if (bestLocation != null) {
            final LocalLocation localLocation = new LocalLocation(bestLocation);
            if (DEBUG) {
                MyLog.d(CLS_NAME, "updateLocation:" + localLocation.mLatitudeDegrees + ", " + localLocation.mLongitudeDegrees);
            }
            LocationRepository.setLastLocation(mContext, localLocation);
            return localLocation;
        } else if (DEBUG) {
            MyLog.d(CLS_NAME, "no location");
        }
        return null;
    }

    /**
     * Stop using GPS/Network/Fused listener
     */
    private void stopLocation() {
        mLocationManager.removeUpdates(mLocationListener);
        mIsLocationByGpsRegistered = false;
        mIsLocationByNetworkRegistered = false;
        mIsLocationByFusedRegistered = false;
        if (DEBUG) {
            MyLog.d(CLS_NAME, "stopLocation");
        }
    }

    @Override
    @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
    public @NonNull Single<LocalLocation> getCurrentLocation(final boolean fallback2Local, @IntRange(from = 0) int timeout, @NonNull TimeUnit unit) {
        return Single.create((SingleOnSubscribe<LocalLocation>) emitter -> {
            mLocationListener.addEmitter(emitter);
            try {
                requestLocation();
            } catch (Throwable t) {
                if (!emitter.tryOnError(t) && DEBUG) {
                    MyLog.w(CLS_NAME, "getCurrentLocation:" + t.getClass().getSimpleName() + ", " + t.getMessage());
                }
            }
            emitter.setCancellable(() -> {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "getCurrentLocation cancelled");
                }
                final boolean noEmitter = mLocationListener.removeEmitter(emitter);
                if (noEmitter) {
                    stopLocation();
                }
            });
        }).timeout(timeout, unit, Schedulers.single(), Single.defer((Supplier<SingleSource<LocalLocation>>) () -> Single.create((SingleOnSubscribe<LocalLocation>) emitter -> {
            try {
                LocalLocation currentLocation = getLastKnownLocation();
                if (currentLocation == null) {
                    if (fallback2Local) {
                        currentLocation = getLastLocation();
                    } else {
                        currentLocation = getDefaultLocation();
                    }
                }
                if (!emitter.isDisposed()) {
                    emitter.onSuccess(currentLocation);
                }
            } catch (Throwable t) {
                if (!emitter.tryOnError(t) && DEBUG) {
                    MyLog.w(CLS_NAME, "getLastKnownLocation:" + t.getClass().getSimpleName() + ", " + t.getMessage());
                }
            }
        })));
    }

    public @NonNull LocalLocation getLastLocation() {
        LocalLocation lastLocation = LocationRepository.getLastLocation(mContext);
        if (lastLocation == null) {
            lastLocation = getDefaultLocation();
        }
        return lastLocation;
    }

    static final class RxLocationListener implements LocationListener {
        private final LocationCallbackAdapter mLocationCallbackAdapter;
        public RxLocationListener(Context context) {
            this.mLocationCallbackAdapter = new LocationCallbackAdapter(context);
        }

        public void addEmitter(@NonNull SingleEmitter<LocalLocation> emitter) {
            this.mLocationCallbackAdapter.addEmitter(emitter);
        }

        public boolean removeEmitter(@NonNull SingleEmitter<LocalLocation> emitter) {
            return this.mLocationCallbackAdapter.removeEmitter(emitter);
        }

        @Override
        public void onLocationChanged(@Nullable Location location) {
            if (location == null) {
                return;
            }
            if (!TextUtils.isEmpty(location.getProvider())) {
                switch (location.getProvider()) {
                    case LocationManager.GPS_PROVIDER:
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "GPS location");
                        }
                        break;
                    case LocationManager.NETWORK_PROVIDER:
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "net location");
                        }
                        break;
                    case LocationManager.FUSED_PROVIDER:
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "fused location");
                        }
                        break;
                    case LocationManager.PASSIVE_PROVIDER:
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "passive location");
                        }
                        break;
                }
            }
            this.mLocationCallbackAdapter.onLocationChanged(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
        }
    }
}
