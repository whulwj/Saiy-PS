package ai.saiy.android.command.orientation;

import android.content.Context;
import android.provider.Settings;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import ai.saiy.android.utils.MyLog;

public class OrientationHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = OrientationHelper.class.getSimpleName();

    public int getRotation(Context context) {
        switch (((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return Surface.ROTATION_270;
            case Surface.ROTATION_180:
                return Surface.ROTATION_180;
            case Surface.ROTATION_90:
                return Surface.ROTATION_90;
            case Surface.ROTATION_0:
            default:
                return Surface.ROTATION_0;
        }
    }

    public boolean somersault(Context context, @NonNull CommandOrientationValues commandOrientationValues) {
        switch (getRotation(context)) {
            case Surface.ROTATION_270:
                if (!freezeAutoRotation(context)) {
                    return false;
                }
                if (commandOrientationValues.getType() == CommandOrientationValues.Type.SOMERSAULT_FORWARD) {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
                    waitForUserRotation();
                } else {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
                    waitForUserRotation();
                }
                thawAutoRotation(context);
                return true;
            case Surface.ROTATION_180:
                if (!freezeAutoRotation(context)) {
                    return false;
                }
                if (commandOrientationValues.getType() == CommandOrientationValues.Type.SOMERSAULT_FORWARD) {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
                    waitForUserRotation();
                } else {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
                    waitForUserRotation();
                }
                thawAutoRotation(context);
                return true;
            case Surface.ROTATION_90:
                if (!freezeAutoRotation(context)) {
                    return false;
                }
                if (commandOrientationValues.getType() == CommandOrientationValues.Type.SOMERSAULT_FORWARD) {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
                    waitForUserRotation();
                } else {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
                    waitForUserRotation();
                }
                thawAutoRotation(context);
                return true;
            case Surface.ROTATION_0:
            default:
                if (!freezeAutoRotation(context)) {
                    return false;
                }
                if (commandOrientationValues.getType() == CommandOrientationValues.Type.SOMERSAULT_FORWARD) {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    waitForUserRotation();
                } else {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_180);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_90);
                    waitForUserRotation();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
                    waitForUserRotation();
                }
                thawAutoRotation(context);
                return true;
        }
    }

    public boolean isAutoRotationFrozen(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 0;
    }

    public void thawAutoRotation(Context context) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }
    }

    public boolean freezeAutoRotation(Context context) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "Exception");
                e.printStackTrace();
            }
            return false;
        }
    }

    public void fixedToUserRotation(Context context) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
            Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, getRotation(context));
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "Exception");
                e.printStackTrace();
            }
        }
    }

    private void waitForUserRotation() {
        try {
            Thread.sleep(850L);
        } catch (InterruptedException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "InterruptedException");
                e.printStackTrace();
            }
        }
    }
}
