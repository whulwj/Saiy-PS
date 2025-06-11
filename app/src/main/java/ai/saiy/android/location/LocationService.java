package ai.saiy.android.location;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.text.format.DateUtils;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import java.util.concurrent.TimeUnit;

import ai.saiy.android.utils.MyLog;
import io.reactivex.rxjava3.core.Single;

/**
 * Abstract Location Service
 */
public interface LocationService {
    String FAKE_PROVIDER = "";
    // After rebooting of device, the first successful network-location takes more than 25s.
    int LOCATION_REQUEST_TIME_OUT = (int) DateUtils.MINUTE_IN_MILLIS;
    int LOCAL_LOCATION_REQUEST_TIME_OUT = 250;
    boolean DEBUG = MyLog.DEBUG;
    String CLS_NAME = "LocationService";

    @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
    @NonNull Single<LocalLocation> getCurrentLocation(final boolean fallback2Local, @IntRange(from = 0) int timeout, @NonNull TimeUnit unit);
    default @NonNull LocalLocation getDefaultLocation() {
        return new LocalLocation(FAKE_PROVIDER);
    }
    @NonNull
    LocalLocation getLastLocation();
}
