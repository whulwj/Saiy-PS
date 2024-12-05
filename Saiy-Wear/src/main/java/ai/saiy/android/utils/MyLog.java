package ai.saiy.android.utils;

import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;

public final class MyLog {

    /**
     * Prevent instantiation
     */
    public MyLog() {
        throw new IllegalArgumentException(Resources.getSystem().getString(android.R.string.no));
    }

    /* Set to false in production. All Classes check this */
    public static final boolean DEBUG = true;

    private static final String TAG = "SAIY-WEAR";

    public static void d(@NonNull final String clsName, @NonNull final String message) {
        Log.d(TAG, clsName + ": " + message);
    }

    public static void v(@NonNull final String clsName, @NonNull final String message) {
        Log.v(TAG, clsName + ": " + message);
    }

    public static void i(@NonNull final String clsName, @NonNull final String message) {
        Log.i(TAG, clsName + ": " + message);
    }

    public static void w(@NonNull final String clsName, @NonNull final String message) {
        Log.w(TAG, clsName + ": " + message);
    }

    public static void e(@NonNull final String clsName, @NonNull final String message) {
        Log.e(TAG, clsName + ": " + message);
    }
}
