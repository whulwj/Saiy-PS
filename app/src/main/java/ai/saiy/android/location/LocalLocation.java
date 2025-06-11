package ai.saiy.android.location;

import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 本地位置(简陋版)
 *
 * @see Location
 */
public final class LocalLocation {
    private static final int HAS_ALTITUDE_MASK = 1;
    private static final int HAS_SPEED_MASK = 1 << 1;
    private static final int HAS_BEARING_MASK = 1 << 2;
    public static final int HAS_HORIZONTAL_ACCURACY_MASK = 1 << 3;
    private static final int HAS_MOCK_PROVIDER_MASK = 1 << 4;
    private static final int HAS_ALTITUDE_ACCURACY_MASK = 1 << 5;
    private static final int HAS_SPEED_ACCURACY_MASK = 1 << 6;
    private static final int HAS_BEARING_ACCURACY_MASK = 1 << 7;
    private static final int HAS_ELAPSED_REALTIME_UNCERTAINTY_MASK = 1 << 8;
    private static final int HAS_MSL_ALTITUDE_MASK = 1 << 9;
    private static final int HAS_MSL_ALTITUDE_ACCURACY_MASK = 1 << 10;

    //Default in New York
    public static final double DEFAULT_LATITUDE = 40.73;
    public static final double DEFAULT_LONGITUDE = 73.93;

    public static final String KEY_FIELDS_MASK = "fieldsMask";
    public static final String KEY_PROVIDER = "provider";
    public static final String KEY_TIME = "time";
    public static final String KEY_ELAPSED_REALTIME = "elapsedRealtime";
    public static final String KEY_ELAPSED_REALTIME_UNCERTAINTY = "elapsedRealtimeUncertainty";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_HORIZONTAL_ACCURACY = "horizontalAccuracy";
    public static final String KEY_ALTITUDE = "altitude";
    public static final String KEY_ALTITUDE_ACCURACY = "altitudeAccuracy";
    public static final String KEY_SPEED_PER_SECOND = "speedPerSecond";
    public static final String KEY_SPEED_ACCURACY_PER_SECOND = "speedAccuracyPerSecond";
    public static final String KEY_BEARING = "bearing";
    public static final String KEY_BEARING_ACCURACY = "bearingAccuracy";
    public static final String KEY_MSL_ALTITUDE = "mslAltitude";
    public static final String KEY_MSL_ALTITUDE_ACCURACY = "mslAltitudeAccuracy";
    public static final String KEY_EXTRAS = "extras";

    // A bitmask of fields present in this object (see HAS_* constants defined above).
    public int mFieldsMask = 0;

    public final @Nullable String mProvider; //来源
    public final long mTimeMs; //时间戳
    public final long mElapsedRealtimeNs;
    public final double mElapsedRealtimeUncertaintyNs;
    public final double mLatitudeDegrees; //纬度
    public final double mLongitudeDegrees; //经度
    public final float mHorizontalAccuracyMeters;
    public final double mAltitudeMeters;
    public final float mAltitudeAccuracyMeters;
    public final float mSpeedMetersPerSecond;
    public final float mSpeedAccuracyMetersPerSecond;
    public final float mBearingDegrees;
    public final float mBearingAccuracyDegrees;
    public final double mMslAltitudeMeters;
    public final float mMslAltitudeAccuracyMeters;

    public final Bundle mExtras;

    /**
     * @param provider 数据来源
     */
    public LocalLocation(@Nullable String provider) {
        this.mProvider = provider;
        this.mFieldsMask = 0;
        this.mTimeMs = System.currentTimeMillis();
        this.mElapsedRealtimeNs = SystemClock.elapsedRealtimeNanos();
        this.mElapsedRealtimeUncertaintyNs = 0;
        this.mLatitudeDegrees = LocalLocation.DEFAULT_LATITUDE;
        this.mLongitudeDegrees = LocalLocation.DEFAULT_LONGITUDE;
        this.mHorizontalAccuracyMeters = 0;
        this.mAltitudeMeters = 0;
        this.mAltitudeAccuracyMeters = 0;
        this.mSpeedMetersPerSecond = 0;
        this.mSpeedAccuracyMetersPerSecond = 0;
        this.mBearingDegrees = 0;
        this.mBearingAccuracyDegrees = 0;
        this.mMslAltitudeMeters = 0;
        this.mMslAltitudeAccuracyMeters = 0;
        this.mExtras = null;
    }

    /**
     * @param provider  数据来源
     * @param timeMs    时间戳
     * @param latitude  纬度
     * @param longitude 经度
     */
    public LocalLocation(@Nullable String provider, int fieldMask, long timeMs, long elapsedRealtimeNs, double elapsedRealtimeUncertaintyNs, double latitude, double longitude,
                         float horizontalAccuracyMeters, double altitudeMeters, float altitudeAccuracyMeters, float speedMetersPerSecond, float speedAccuracyMetersPerSecond,
                         float bearingDegrees, float bearingAccuracyDegrees, double mslAltitudeMeters, float mslAltitudeAccuracyMeters, Bundle extras) {
        this.mProvider = provider;
        this.mFieldsMask = fieldMask;
        this.mTimeMs = timeMs;
        this.mElapsedRealtimeNs = elapsedRealtimeNs;
        this.mElapsedRealtimeUncertaintyNs = elapsedRealtimeUncertaintyNs;
        this.mLatitudeDegrees = latitude;
        this.mLongitudeDegrees = longitude;
        this.mHorizontalAccuracyMeters = horizontalAccuracyMeters;
        this.mAltitudeMeters = altitudeMeters;
        this.mAltitudeAccuracyMeters = altitudeAccuracyMeters;
        this.mSpeedMetersPerSecond = speedMetersPerSecond;
        this.mSpeedAccuracyMetersPerSecond = speedAccuracyMetersPerSecond;
        this.mBearingDegrees = bearingDegrees;
        this.mBearingAccuracyDegrees = bearingAccuracyDegrees;
        this.mMslAltitudeMeters = mslAltitudeMeters;
        this.mMslAltitudeAccuracyMeters = mslAltitudeAccuracyMeters;
        this.mExtras = extras;
    }

    public LocalLocation(@NonNull Location location) {
        if (location.hasAccuracy()) {
            mFieldsMask |= HAS_HORIZONTAL_ACCURACY_MASK;
        }
        if (location.hasAltitude()) {
            mFieldsMask |= HAS_ALTITUDE_MASK;
        }
        if (location.hasSpeed()) {
            mFieldsMask |= HAS_SPEED_MASK;
        }
        if (location.hasBearing()) {
            mFieldsMask |= HAS_BEARING_MASK;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (location.hasVerticalAccuracy()) {
                mFieldsMask |= HAS_ALTITUDE_ACCURACY_MASK;
            }
            if (location.hasSpeedAccuracy()) {
                mFieldsMask |= HAS_SPEED_ACCURACY_MASK;
            }
            if (location.hasBearingAccuracy()) {
                mFieldsMask |= HAS_BEARING_ACCURACY_MASK;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (location.hasElapsedRealtimeUncertaintyNanos()) {
                    mFieldsMask |= HAS_ELAPSED_REALTIME_UNCERTAINTY_MASK;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (location.isMock()) {
                        mFieldsMask |= HAS_MOCK_PROVIDER_MASK;
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        if (location.hasMslAltitude()) {
                            mFieldsMask |= HAS_MSL_ALTITUDE_MASK;
                        }
                        if (location.hasMslAltitudeAccuracy()) {
                            mFieldsMask |= HAS_MSL_ALTITUDE_ACCURACY_MASK;
                        }
                    }
                }
            }
        }
        mProvider = location.getProvider();
        mTimeMs = location.getTime();
        mElapsedRealtimeNs = location.getElapsedRealtimeNanos();
        mLatitudeDegrees = location.getLatitude();
        mLongitudeDegrees = location.getLongitude();
        mHorizontalAccuracyMeters = location.getAccuracy();
        mAltitudeMeters = location.getAltitude();
        mSpeedMetersPerSecond = location.getSpeed();
        mBearingDegrees = location.getBearing();
        mExtras = (location.getExtras() == null) ? null : new Bundle(location.getExtras());

        float altitudeAccuracyMeters = 0, speedAccuracyMetersPerSecond = 0, bearingAccuracyDegrees = 0, mslAltitudeAccuracyMeters = 0;
        double elapsedRealtimeUncertaintyNs = 0, mslAltitudeMeters = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            altitudeAccuracyMeters = location.getVerticalAccuracyMeters();
            speedAccuracyMetersPerSecond = location.getSpeedAccuracyMetersPerSecond();
            bearingAccuracyDegrees = location.getBearingAccuracyDegrees();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                elapsedRealtimeUncertaintyNs = location.getElapsedRealtimeUncertaintyNanos();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    if (location.hasMslAltitude()) {
                        mslAltitudeMeters = location.getMslAltitudeMeters();
                    }
                    if (location.hasMslAltitudeAccuracy()) {
                        mslAltitudeAccuracyMeters = location.getMslAltitudeAccuracyMeters();
                    }
                }
            }
        }
        mAltitudeAccuracyMeters = altitudeAccuracyMeters;
        mSpeedAccuracyMetersPerSecond = speedAccuracyMetersPerSecond;
        mBearingAccuracyDegrees = bearingAccuracyDegrees;
        mElapsedRealtimeUncertaintyNs = elapsedRealtimeUncertaintyNs;
        mMslAltitudeMeters = mslAltitudeMeters;
        mMslAltitudeAccuracyMeters = mslAltitudeAccuracyMeters;
    }

    /**
     * Returns the Unix epoch time of this location fix, in milliseconds since the start of the Unix
     * epoch (00:00:00 January 1, 1970 UTC).
     *
     * <p>There is no guarantee that different locations have times set from the same clock.
     * Locations derived from the {@link android.location.LocationManager#GPS_PROVIDER} are guaranteed to have their
     * time originate from the clock in use by the satellite constellation that provided the fix.
     * Locations derived from other providers may use any clock to set their time, though it is most
     * common to use the device's Unix epoch time system clock (which may be incorrect).
     *
     * <p>Note that the device's Unix epoch time system clock is not monotonic; it can jump forwards
     * or backwards unpredictably and may be changed at any time by the user, so this time should
     * not be used to order or compare locations. Prefer {@link #getElapsedRealtimeNanos} for that
     * purpose, as the elapsed realtime clock is guaranteed to be monotonic.
     *
     * <p>On the other hand, this method may be useful for presenting a human-readable time to the
     * user, or as a heuristic for comparing location fixes across reboot or across devices.
     *
     * <p>All locations generated by the {@link android.location.LocationManager} are guaranteed to have this time
     * set, however remember that the device's system clock may have changed since the location was
     * generated.
     *
     * @return the Unix epoch time of this location
     */
    public @IntRange(from = 0) long getTime() {
        return mTimeMs;
    }

    /**
     * Return the time of this fix in nanoseconds of elapsed realtime since system boot.
     *
     * <p>This value can be compared with {@link SystemClock#elapsedRealtimeNanos} to
     * reliably order or compare locations. This is reliable because elapsed realtime is guaranteed
     * to be monotonic and continues to increment even when the system is in deep sleep (unlike
     * {@link #getTime}). However, since elapsed realtime is with reference to system boot, it does
     * not make sense to use this value to order or compare locations across boot cycles or devices.
     *
     * <p>All locations generated by the {@link android.location.LocationManager} are guaranteed to have a valid
     * elapsed realtime set.
     *
     * @return elapsed realtime of this location in nanoseconds
     */
    public @IntRange(from = 0) long getElapsedRealtimeNanos() {
        return mElapsedRealtimeNs;
    }

    /**
     * Return the time of this fix in milliseconds of elapsed realtime since system boot.
     *
     * @return elapsed realtime of this location in milliseconds
     * @see #getElapsedRealtimeNanos()
     */
    public @IntRange(from = 0) long getElapsedRealtimeMillis() {
        return TimeUnit.NANOSECONDS.toMillis(mElapsedRealtimeNs);
    }

    /**
     * Returns true if this location has a horizontal accuracy, false otherwise.
     */
    public boolean hasAccuracy() {
        return (mFieldsMask & HAS_HORIZONTAL_ACCURACY_MASK) != 0;
    }

    /**
     * Returns true if this location has an altitude, false otherwise.
     */
    public boolean hasAltitude() {
        return (mFieldsMask & HAS_ALTITUDE_MASK) != 0;
    }

    /**
     * True if this location has a bearing, false otherwise.
     */
    public boolean hasBearing() {
        return (mFieldsMask & HAS_BEARING_MASK) != 0;
    }

    /**
     * Returns true if this location has a bearing accuracy, false otherwise.
     */
    public boolean hasBearingAccuracy() {
        return (mFieldsMask & HAS_BEARING_ACCURACY_MASK) != 0;
    }

    /**
     * Returns true if this location has a Mean Sea Level altitude, false otherwise.
     */
    public boolean hasMslAltitude() {
        return (mFieldsMask & HAS_MSL_ALTITUDE_MASK) != 0;
    }

    /**
     * Returns true if this location has a Mean Sea Level altitude accuracy, false otherwise.
     */
    public boolean hasMslAltitudeAccuracy() {
        return (mFieldsMask & HAS_MSL_ALTITUDE_ACCURACY_MASK) != 0;
    }

    /**
     * True if this location has a speed, false otherwise.
     */
    public boolean hasSpeed() {
        return (mFieldsMask & HAS_SPEED_MASK) != 0;
    }

    /**
     * Returns true if this location has a speed accuracy, false otherwise.
     */
    public boolean hasSpeedAccuracy() {
        return (mFieldsMask & HAS_SPEED_ACCURACY_MASK) != 0;
    }

    /**
     * Returns true if this location has a vertical accuracy, false otherwise.
     */
    public boolean hasVerticalAccuracy() {
        return (mFieldsMask & HAS_ALTITUDE_ACCURACY_MASK) != 0;
    }

    /**
     * Returns true if this location is marked as a mock location. If this location comes from the
     * Android framework, this indicates that the location was provided by a test location provider,
     * and thus may not be related to the actual location of the device.
     */
    public boolean isMock() {
        return (mFieldsMask & HAS_MOCK_PROVIDER_MASK) != 0;
    }

    public double getLatitude() {
        return this.mLatitudeDegrees;
    }

    public double getLongitude() {
        return this.mLongitudeDegrees;
    }

    public boolean isFake() {
        return TextUtils.equals(LocationService.FAKE_PROVIDER, this.mProvider) && UtilsLocatio.isNearBy(this.mLatitudeDegrees, this.mLongitudeDegrees, LocalLocation.DEFAULT_LATITUDE, LocalLocation.DEFAULT_LONGITUDE);
    }

    @Override
    public @NonNull String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Location[");
        s.append(mProvider);
        s.append(" ").append(String.format(Locale.ROOT, "%.6f,%.6f", mLatitudeDegrees,
                mLongitudeDegrees));
        if (hasAccuracy()) {
            s.append(" hAcc=").append(mHorizontalAccuracyMeters);
        }
        s.append(" et=");
        s.append(getElapsedRealtimeMillis());
//        androidx.core.util.TimeUtils.formatDuration(getElapsedRealtimeMillis(), s);
        if (hasAltitude()) {
            s.append(" alt=").append(mAltitudeMeters);
            if (hasVerticalAccuracy()) {
                s.append(" vAcc=").append(mAltitudeAccuracyMeters);
            }
        }
        if (hasMslAltitude()) {
            s.append(" mslAlt=").append(mMslAltitudeMeters);
            if (hasMslAltitudeAccuracy()) {
                s.append(" mslAltAcc=").append(mMslAltitudeAccuracyMeters);
            }
        }
        if (hasSpeed()) {
            s.append(" vel=").append(mSpeedMetersPerSecond);
            if (hasSpeedAccuracy()) {
                s.append(" sAcc=").append(mSpeedAccuracyMetersPerSecond);
            }
        }
        if (hasBearing()) {
            s.append(" bear=").append(mBearingDegrees);
            if (hasBearingAccuracy()) {
                s.append(" bAcc=").append(mBearingAccuracyDegrees);
            }
        }
        if (isMock()) {
            s.append(" mock");
        }

        if (mExtras != null && !mExtras.isEmpty()) {
            s.append(" {").append(mExtras).append('}');
        }
        s.append(']');
        return s.toString();
    }
}
