package ai.saiy.android.location;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.content.Context;
import android.location.Location;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.annotation.WorkerThread;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import ai.saiy.android.utils.MyLog;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Native Location Service of google location API
 * <a href="https://developer.android.google.cn/develop/sensors-and-location/location/migration" />
 * <a href="https://dopebase.com/android-location-tracking-kotlin-best-practices" />
 */
public final class GoogleLocationService implements LocationService {
    private final Context mContext;
    private final FusedLocationProviderClient mFusedLocationClient;

    public GoogleLocationService(Context context) {
        this.mContext = context;
        this.mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
    @WorkerThread
    private @Nullable LocalLocation getLastKnownLocation() {
        final Task<Location> task = mFusedLocationClient.getLastLocation();
        try {
            // Block on a task and get the result synchronously. This is generally done
            // when executing a task inside a separately managed background thread. Doing this
            // on the main (UI) thread can cause your application to become unresponsive.
            final Location lastLocation = Tasks.await(task);
            if (lastLocation != null) {
                final LocalLocation localLocation = new LocalLocation(lastLocation);
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "updateLocation:" + localLocation.mLatitudeDegrees + ", " + localLocation.mLongitudeDegrees);
                }
                LocationRepository.setLastLocation(mContext, localLocation);
                return localLocation;
            } else if (DEBUG) {
                MyLog.d(CLS_NAME, "no location");
            }
            return null;
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

    @Override
    @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
    public @NonNull Single<LocalLocation> getCurrentLocation(final boolean fallback2Local, @IntRange(from = 0) int timeout, @NonNull TimeUnit unit) {
        return Single.create((SingleOnSubscribe<LocalLocation>) emitter -> {
            try {
                final CurrentLocationRequest currentLocationRequest = new CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY).setDurationMillis(unit.toMillis(timeout)).build();
                final Task<Location> task = mFusedLocationClient.getCurrentLocation(currentLocationRequest, (new CancellationTokenSource()).getToken());
                task.addOnCompleteListener(locationTask -> {
                    if (locationTask.isCanceled()) {
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "getCurrentLocation canceled");
                        }
                    } else if (locationTask.isSuccessful()) {
                        final Location currentLocation = locationTask.getResult();
                        if (!emitter.isDisposed()) {
                            if (currentLocation != null) {
                                final LocalLocation localLocation = new LocalLocation(currentLocation);
                                if (DEBUG) {
                                    MyLog.d(CLS_NAME, "currentLocation:" + localLocation.mLatitudeDegrees + ", " + localLocation.mLongitudeDegrees);
                                }
                                LocationRepository.setLastLocation(mContext, localLocation);
                                emitter.onSuccess(localLocation);
                            }
                        }
                    } else if (locationTask.getException() != null) {
                        if (!emitter.tryOnError(locationTask.getException()) && DEBUG) {
                            MyLog.w(CLS_NAME, "getCurrentLocation:" + locationTask.getException().getClass().getSimpleName() + ", " + locationTask.getException().getMessage());
                        }
                    }
                });
            } catch (Throwable t) {
                if (!emitter.tryOnError(t) && DEBUG) {
                    MyLog.w(CLS_NAME, "getCurrentLocation:" + t.getClass().getSimpleName() + ", " + t.getMessage());
                }
            }
            emitter.setCancellable(() -> {
                if (DEBUG) {
                    MyLog.d(CLS_NAME, "getCurrentLocation cancelled");
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
        LocalLocation localLocation = LocationRepository.getLastLocation(mContext);
        if (localLocation == null) {
            localLocation = getDefaultLocation();
        }
        return localLocation;
    }
}
