package ai.saiy.android.command.driving;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.JsonSyntaxException;

import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsParcelable;
import ai.saiy.android.utils.UtilsString;

public class DrivingProfileHelper {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = DrivingProfileHelper.class.getSimpleName();

    private static DrivingProfile defaultDrivingProfile() {
        return new DrivingProfile(true, true, true, false, false, false);
    }

    public static void enable(Context context) {
        DrivingProfile drivingProfile = getDrivingProfile(context);
        drivingProfile.setEnabled(true);
        save(context, drivingProfile);
    }

    public static void save(Context context, DrivingProfile drivingProfile) {
        if (drivingProfile == null) {
            drivingProfile = defaultDrivingProfile();
        }
        String base64String = UtilsParcelable.parcelable2String(drivingProfile);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "save: base64String: " + base64String);
        }
        ai.saiy.android.utils.SPH.setDrivingProfile(context, base64String);
    }

    public static void disable(Context context) {
        DrivingProfile drivingProfile = getDrivingProfile(context);
        drivingProfile.setEnabled(false);
        save(context, drivingProfile);
    }

    public static boolean shouldStartAutomatically(Context context) {
        DrivingProfile drivingProfile = getCheckedDrivingProfile(context);
        return drivingProfile.shouldStartAutomatically();
    }

    public static boolean shouldStopAutomatically(Context context) {
        DrivingProfile drivingProfile = getCheckedDrivingProfile(context);
        return drivingProfile.shouldStopAutomatically();
    }

    public static boolean isAutomatic(Context context) {
        DrivingProfile drivingProfile = getCheckedDrivingProfile(context);
        return (drivingProfile.shouldStopAutomatically() || drivingProfile.shouldStartAutomatically());
    }

    public static boolean isEnabled(Context context) {
        DrivingProfile drivingProfile = getCheckedDrivingProfile(context);
        return drivingProfile.isEnabled();
    }

    public static boolean isStartHotwordEnabled(Context context) {
        DrivingProfile drivingProfile = getCheckedDrivingProfile(context);
        return drivingProfile.isEnabled() && drivingProfile.getStartHotword();
    }

    public static boolean isAnnounceNotificationsEnabled(Context context) {
        DrivingProfile drivingProfile = getCheckedDrivingProfile(context);
        return drivingProfile.isEnabled() && drivingProfile.getAnnounceNotifications();
    }

    public static DrivingProfile getDrivingProfile(Context context) {
        if (haveDriveProfile(context)) {
            try {
                DrivingProfile profile = UtilsParcelable.unmarshall(ai.saiy.android.utils.SPH.getDrivingProfile(context), DrivingProfile.CREATOR);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "profile: " + profile);
                }
                return profile;
            } catch (JsonSyntaxException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "JsonSyntaxException");
                    e.printStackTrace();
                }
            } catch (NullPointerException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "NullPointerException");
                    e.printStackTrace();
                }
            } catch (Exception e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "Exception");
                    e.printStackTrace();
                }
            }
        }
        return defaultDrivingProfile();
    }

    private static boolean haveDriveProfile(Context context) {
        return ai.saiy.android.utils.SPH.getDrivingProfile(context) != null;
    }

    private static @NonNull DrivingProfile getCheckedDrivingProfile(Context context) {
        return UtilsString.notNaked(ai.saiy.android.utils.SPH.getDrivingProfile(context)) ? getDrivingProfile(context) : defaultDrivingProfile();
    }
}
