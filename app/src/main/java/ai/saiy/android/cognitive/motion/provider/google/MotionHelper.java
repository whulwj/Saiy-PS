/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.cognitive.motion.provider.google;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.location.DetectedActivity;
import com.google.gson.JsonSyntaxException;

import ai.saiy.android.command.driving.DrivingProfileHelper;
import ai.saiy.android.service.helper.LocalRequest;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsParcelable;

/**
 * Helper class to store the most recent ActivityRecognition event in the user's shared preferences
 * <p>
 * Created by benrandall76@gmail.com on 15/08/2016.
 */
public class MotionHelper {

    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = MotionHelper.class.getSimpleName();
    private static final long DEFAULT_INACTIVITY_TIMEOUT = 900000L;

    /**
     * Check if we have a recent {@link Motion} object stored
     *
     * @param ctx the application context
     * @return true if a {@link Motion} is stored
     */
    public static boolean haveMotion(@NonNull final Context ctx) {
        return SPH.getMotion(ctx) != null;
    }


    /**
     * Store the {@link Motion} object in the shared preferences
     *
     * @param ctx    the application context
     * @param motion {@link Motion} object
     */
    public static void setMotion(@NonNull final Context ctx, @NonNull final Motion motion) {
        final String base64String = UtilsParcelable.parcelable2String(motion);
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setMotion: base64String: " + base64String);
        }

        SPH.setMotion(ctx, base64String);
        reactMotion(ctx, motion);
    }

    /**
     * Check if we need to react to the detected ActivityRecognition type.
     *
     * @param ctx    the application context
     * @param motion the detection {@link Motion} object
     */
    private static void reactMotion(@NonNull final Context ctx, @NonNull final Motion motion) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "reactMotion");
        }

        if (motion.getType() == DetectedActivity.IN_VEHICLE) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "reactMotion: IN_VEHICLE");
            }
            if (!DrivingProfileHelper.isEnabled(ctx) && DrivingProfileHelper.shouldStartAutomatically(ctx)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "reactMotion: IN_VEHICLE: starting driving profile");
                }
                if (isOverDrivingCooldownThreshold(ctx, DEFAULT_INACTIVITY_TIMEOUT)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "reactMotion: over driving cooldown threshold");
                    }
                    final LocalRequest request = new LocalRequest(ctx);
                    request.prepareDefault(LocalRequest.ACTION_TOGGLE_DRIVING_PROFILE, null);
                    request.execute();
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "reactMotion: within driving cooldown threshold");
                }
            } else if (SPH.getHotwordStartDriving(ctx)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "reactMotion: IN_VEHICLE: starting hotword");
                }
                if (isOverDrivingCooldownThreshold(ctx, DEFAULT_INACTIVITY_TIMEOUT)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "reactMotion: over driving cooldown threshold");
                    }
                    final LocalRequest request = new LocalRequest(ctx);
                    request.prepareDefault(LocalRequest.ACTION_START_HOTWORD, null);
                    request.execute();
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "reactMotion: within driving cooldown threshold");
                }
            } else if (DEBUG) {
                MyLog.i(CLS_NAME, "reactMotion: IN_VEHICLE: hotword/driving profile not automatic");
            }
            SPH.setLastDrivingTime(ctx, System.currentTimeMillis());
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "reactMotion: OTHER");
            }
            if (DrivingProfileHelper.isEnabled(ctx) && DrivingProfileHelper.shouldStopAutomatically(ctx)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "reactMotion: OTHER: handle driving profile automatically: enabled");
                }
                if (!isOverDrivingThreshold(ctx, DEFAULT_INACTIVITY_TIMEOUT)) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "reactMotion: within driving threshold");
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "reactMotion: over driving threshold");
                    }
                    final LocalRequest request = new LocalRequest(ctx);
                    request.prepareDefault(LocalRequest.ACTION_TOGGLE_DRIVING_PROFILE, null);
                    request.execute();
                }
                return;
            }
            if (!SPH.getHotwordStopDriving(ctx)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "checkDrivingProfile: OTHER: handle driving/hotword automatically: disabled");
                }
                return;
            }

            if (DEBUG) {
                MyLog.i(CLS_NAME, "reactMotion: OTHER: handle hotword automatically: enabled");
            }
            if (!isOverDrivingThreshold(ctx, DEFAULT_INACTIVITY_TIMEOUT)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "reactMotion: within driving threshold");
                }
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "reactMotion: over driving threshold");
                }
                final LocalRequest request = new LocalRequest(ctx);
                request.prepareDefault(LocalRequest.ACTION_STOP_HOTWORD, null);
                request.execute();
            }
        }
    }

    /**
     * Get the {@link Motion} we have stored
     *
     * @param ctx the application context
     * @return the {@link Motion} object or a created one if none was present
     */
    public static Motion getMotion(@NonNull final Context ctx) {

        final Motion motion;

        if (haveMotion(ctx)) {
            try {
                motion = UtilsParcelable.unmarshall(SPH.getMemory(ctx), Motion.CREATOR);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "motion: " + motion);
                }
                return motion;
            } catch (final JsonSyntaxException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "motion: JsonSyntaxException");
                    e.printStackTrace();
                }
            } catch (final NullPointerException e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "motion: NullPointerException");
                    e.printStackTrace();
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "motion: Exception");
                    e.printStackTrace();
                }
            }
        }

        return getUnknown();
    }

    /**
     * No {@link Motion} has been stored, so return an empty {@link Motion} object that can be identified as unknown.
     *
     * @return a constructed {@link Motion} object
     */
    private static Motion getUnknown() {
        return new Motion(DetectedActivity.UNKNOWN, 0, 0L);
    }

    public static boolean isOverDrivingThreshold(Context context, long timeout) {
        return System.currentTimeMillis() > SPH.getLastDrivingTime(context) + timeout;
    }

    public static boolean isOverDrivingCooldownThreshold(Context context, long timeout) {
        return System.currentTimeMillis() > SPH.getDrivingCooldownTime(context) + timeout;
    }
}
