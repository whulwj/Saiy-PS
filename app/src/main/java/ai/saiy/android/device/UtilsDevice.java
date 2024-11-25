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

package ai.saiy.android.device;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;

import ai.saiy.android.command.driving.DrivingProfileHelper;
import ai.saiy.android.command.helper.CC;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;

/**
 * A collection of handy device related methods. Static for easy access
 * <p>
 * Created by benrandall76@gmail.com on 06/09/2016.
 */

public class UtilsDevice {
    private static final boolean DEBUG = MyLog.DEBUG;
    private static final String CLS_NAME = UtilsDevice.class.getSimpleName();

    /**
     * Check if the device is currently locked
     *
     * @return true if the device is in secure mode, false otherwise
     */
    public static boolean isDeviceLocked(@NonNull final Context ctx) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 ? ((KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE)).isDeviceLocked() : ((KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked() && ((KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardSecure();
    }

    public static boolean satisfySecureConditions(Context context, CC cc, ai.saiy.android.command.helper.CommandRequest cr) {
        boolean headsetSecure;
        if (SPH.getOverrideSecureHeadset(context)) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            headsetSecure = audioManager.isBluetoothA2dpOn() || audioManager.isWiredHeadsetOn();
        } else {
            headsetSecure = false;
        }
        boolean drivingSecure = SPH.getOverrideSecureDriving(context) && (DrivingProfileHelper.isEnabled(context) || !ai.saiy.android.cognitive.motion.provider.google.MotionHelper.isOverDrivingThreshold(context, 300000L));
        if (DEBUG) {
            MyLog.i(CLS_NAME, "satisfySecureConditions: getOverrideSecureHeadset: " + SPH.getOverrideSecureHeadset(context));
            MyLog.i(CLS_NAME, "satisfySecureConditions: headsetSecure: " + headsetSecure);
            MyLog.i(CLS_NAME, "satisfySecureConditions: getOverrideSecureDriving: " + SPH.getOverrideSecureDriving(context));
            MyLog.i(CLS_NAME, "satisfySecureConditions: DrivingProfileHelper.isProfileEnabled: " + DrivingProfileHelper.isEnabled(context));
            MyLog.i(CLS_NAME, "satisfySecureConditions: MotionHelper.isOverDrivingThreshold: " + ai.saiy.android.cognitive.motion.provider.google.MotionHelper.isOverDrivingThreshold(context, 300000L));
            MyLog.i(CLS_NAME, "satisfySecureConditions: drivingSecure: " + drivingSecure);
            MyLog.i(CLS_NAME, "satisfySecureConditions: command.isSecure(): " + cc.isSecure());
            MyLog.i(CLS_NAME, "satisfySecureConditions: cr.wasSecure(): " + cr.wasSecure());
            MyLog.i(CLS_NAME, "satisfySecureConditions: isDeviceLocked: " + isDeviceLocked(context));
            MyLog.i(CLS_NAME, "satisfySecureConditions: getOverrideSecure: " + SPH.getOverrideSecure(context));
        }
        return cc.isSecure() && cr.wasSecure() && isDeviceLocked(context) && !SPH.getOverrideSecure(context) && !headsetSecure && !drivingSecure;
    }

    /**
     * Check if the device screen is currently turned off
     *
     * @param ctx the application context
     * @return true if the screen is off, false otherwise
     */
    public static boolean isScreenOff(@NonNull final Context ctx) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return isScreenOff20(ctx);
        } else {
            return isScreenOffDeprecated(ctx);
        }
    }

    private static boolean isScreenOffDeprecated(@NonNull final Context ctx) {
        return !((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).isScreenOn();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private static boolean isScreenOff20(@NonNull final Context ctx) {
        return !((PowerManager) ctx.getSystemService(Context.POWER_SERVICE)).isInteractive();
    }
}
