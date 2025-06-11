package ai.saiy.android.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.util.HashSet;
import java.util.Set;

import ai.saiy.android.utils.MyLog;

@RestrictTo(RestrictTo.Scope.LIBRARY)
abstract class LocationRepository {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = "LocationRepository";

    private static final String LOCATION_PREFERENCE_NAME = "_preferences_location";

    public static LocalLocation getLastLocation(final Context context) {
        final SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + LOCATION_PREFERENCE_NAME, Context.MODE_PRIVATE);
        final String provider = preferences.getString(LocalLocation.KEY_PROVIDER, null);
        if (TextUtils.isEmpty(provider)) {
            return null;
        }
        final int fieldMask = preferences.getInt(LocalLocation.KEY_FIELDS_MASK, 0);
        final long timeMs = preferences.getLong(LocalLocation.KEY_TIME, 0L);
        final long elapsedRealtimeNs = preferences.getLong(LocalLocation.KEY_ELAPSED_REALTIME, 0);
        final double latitude = Double.longBitsToDouble(preferences.getLong(LocalLocation.KEY_LATITUDE, 0));
        final double longitude = Double.longBitsToDouble(preferences.getLong(LocalLocation.KEY_LONGITUDE, 0));
        final float horizontalAccuracyMeters = preferences.getFloat(LocalLocation.KEY_HORIZONTAL_ACCURACY, 0);
        final double altitudeMeter = Double.longBitsToDouble(preferences.getLong(LocalLocation.KEY_ALTITUDE, 0));
        final float speedMetersPerSecond = preferences.getFloat(LocalLocation.KEY_SPEED_PER_SECOND, 0);
        final float bearingDegrees = preferences.getFloat(LocalLocation.KEY_BEARING, 0);
        final Bundle extras = LocationRepository.loadBundle(preferences);
        float altitudeAccuracyMeters = 0, speedAccuracyMetersPerSecond = 0, bearingAccuracyDegrees = 0, mslAltitudeAccuracyMeters = 0;
        double elapsedRealtimeUncertaintyNs = 0, mslAltitudeMeters = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            altitudeAccuracyMeters = preferences.getFloat(LocalLocation.KEY_ALTITUDE_ACCURACY, 0);
            speedAccuracyMetersPerSecond = preferences.getFloat(LocalLocation.KEY_SPEED_ACCURACY_PER_SECOND, 0);
            bearingAccuracyDegrees = preferences.getFloat(LocalLocation.KEY_BEARING_ACCURACY, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                elapsedRealtimeUncertaintyNs = Double.longBitsToDouble(preferences.getLong(LocalLocation.KEY_ELAPSED_REALTIME_UNCERTAINTY, 0));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    mslAltitudeMeters = Double.longBitsToDouble(preferences.getLong(LocalLocation.KEY_MSL_ALTITUDE, 0));
                    mslAltitudeAccuracyMeters = preferences.getFloat(LocalLocation.KEY_MSL_ALTITUDE_ACCURACY, 0);
                }
            }
        }
        return new LocalLocation(provider, fieldMask, timeMs, elapsedRealtimeNs, elapsedRealtimeUncertaintyNs, latitude, longitude,
                horizontalAccuracyMeters, altitudeMeter, altitudeAccuracyMeters, speedMetersPerSecond, speedAccuracyMetersPerSecond,
                bearingDegrees, bearingAccuracyDegrees, mslAltitudeMeters, mslAltitudeAccuracyMeters, extras);
    }

    public static void setLastLocation(final Context context, @NonNull LocalLocation localLocation) {
        final SharedPreferences preferences = context.getSharedPreferences(context.getPackageName() + LOCATION_PREFERENCE_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(LocalLocation.KEY_FIELDS_MASK, localLocation.mFieldsMask);
        editor.putString(LocalLocation.KEY_PROVIDER, localLocation.mProvider);
        editor.putLong(LocalLocation.KEY_TIME, localLocation.mTimeMs);
        editor.putLong(LocalLocation.KEY_ELAPSED_REALTIME, localLocation.mElapsedRealtimeNs);
        editor.putLong(LocalLocation.KEY_LATITUDE, Double.doubleToRawLongBits(localLocation.mLatitudeDegrees));
        editor.putLong(LocalLocation.KEY_LONGITUDE, Double.doubleToRawLongBits(localLocation.mLongitudeDegrees));
        editor.putFloat(LocalLocation.KEY_HORIZONTAL_ACCURACY, localLocation.mHorizontalAccuracyMeters);
        editor.putLong(LocalLocation.KEY_ALTITUDE, Double.doubleToRawLongBits(localLocation.mAltitudeMeters));
        editor.putFloat(LocalLocation.KEY_SPEED_PER_SECOND, localLocation.mSpeedMetersPerSecond);
        editor.putFloat(LocalLocation.KEY_BEARING, localLocation.mBearingDegrees);
        LocationRepository.saveBundle(editor, localLocation.mExtras);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            editor.putFloat(LocalLocation.KEY_ALTITUDE_ACCURACY, localLocation.mAltitudeAccuracyMeters);
            editor.putFloat(LocalLocation.KEY_SPEED_ACCURACY_PER_SECOND, localLocation.mSpeedAccuracyMetersPerSecond);
            editor.putFloat(LocalLocation.KEY_BEARING_ACCURACY, localLocation.mBearingAccuracyDegrees);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                editor.putLong(LocalLocation.KEY_ELAPSED_REALTIME_UNCERTAINTY, Double.doubleToRawLongBits(localLocation.mElapsedRealtimeUncertaintyNs));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    editor.putLong(LocalLocation.KEY_MSL_ALTITUDE, Double.doubleToRawLongBits(localLocation.mMslAltitudeMeters));
                    editor.putFloat(LocalLocation.KEY_MSL_ALTITUDE_ACCURACY, localLocation.mMslAltitudeAccuracyMeters);
                }
            }
        }
        editor.apply();
    }

    private static void saveBundle(SharedPreferences.Editor editor, @Nullable Bundle extras) {
        if (extras == null) {
            editor.remove(LocalLocation.KEY_EXTRAS);
            return;
        }
        final Set<String> keys = extras.keySet();
        final Set<String> availableKeys = new HashSet<>(keys);
        Object value;
        for (String key : keys) {
            if (key == null) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "Unsupported key");
                }
                continue;
            }
            value = extras.get(key);
            if (value instanceof Boolean) {
                editor.putBoolean(ValueType.VALUE_BOOLEAN + key, (Boolean) value);
            } else if (value instanceof Byte) {
                editor.putInt(ValueType.VALUE_BYTE + key, (Byte) value);
            } else if (value instanceof Short) {
                editor.putInt(ValueType.VALUE_SHORT + key, (Short) value);
            } else if (value instanceof Integer) {
                editor.putInt(ValueType.VALUE_INTEGER + key, (Integer) value);
            } else if (value instanceof Long) {
                editor.putLong(ValueType.VALUE_LONG + key, (Long) value);
            } else if (value instanceof Float) {
                editor.putFloat(ValueType.VALUE_FLOAT + key, (Float) value);
            } else if (value instanceof String) {
                editor.putString(ValueType.VALUE_STRING + key, (String) value);
            } else if (value instanceof Double) {
                editor.putLong(ValueType.VALUE_DOUBLE + key, Double.doubleToRawLongBits((Double) value));
            } else if (value != null) {
                availableKeys.remove(key);
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "Unsupported value type:" + value.getClass().getName());
                }
            }
        }
        editor.putStringSet(LocalLocation.KEY_EXTRAS, availableKeys);
    }

    private static Bundle loadBundle(SharedPreferences preferences) {
        final Set<String> keys = preferences.getStringSet(LocalLocation.KEY_EXTRAS, null);
        final Bundle extras = (keys == null) ? null : new Bundle();
        if (keys != null) {
            String valueType, originalKey;
            for (String key : keys) {
                if (TextUtils.isEmpty(key) || key.length() <= 1) {
                    continue;
                }
                valueType = key.substring(0, 1);
                originalKey = key.substring(1);
                switch (valueType) {
                    case ValueType.VALUE_BOOLEAN:
                        extras.putBoolean(originalKey, preferences.getBoolean(key, false));
                        break;
                    case ValueType.VALUE_BYTE:
                        extras.putByte(originalKey, (byte) preferences.getInt(key, 0));
                        break;
                    case ValueType.VALUE_SHORT:
                        extras.putShort(originalKey, (short) preferences.getInt(key, 0));
                        break;
                    case ValueType.VALUE_INTEGER:
                        extras.putInt(originalKey, preferences.getInt(key, 0));
                        break;
                    case ValueType.VALUE_LONG:
                        extras.putLong(originalKey, preferences.getLong(key, 0));
                        break;
                    case ValueType.VALUE_FLOAT:
                        extras.putFloat(originalKey, preferences.getFloat(key, 0));
                        break;
                    case ValueType.VALUE_DOUBLE:
                        extras.putDouble(originalKey, Double.longBitsToDouble(preferences.getLong(key, 0)));
                        break;
                    case ValueType.VALUE_STRING:
                        extras.putString(originalKey, preferences.getString(key, null));
                        break;
                    default:
                        break;
                }
            }
        }
        return extras;
    }
}
