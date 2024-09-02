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

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.PermissionChecker;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import ai.saiy.android.utils.MyLog;

/**
 * Class to setup the ActivityRecognition API and register the {@link MotionIntentService} for callbacks.
 * <p>
 * This must only survive as long as {@link ai.saiy.android.service.SelfAware} is running, so must
 * be destroyed as per its lifecycle.
 * <p>
 * Created by benrandall76@gmail.com on 06/07/2016.
 */

public class MotionRecognition implements ResultCallback<Status> {

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = MotionRecognition.class.getSimpleName();

    private Context mContext;
    private PendingIntent pendingIntent;
    private ActivityRecognitionClient activityRecognitionClient;

    /**
     * Prepare the Activity Recognition API for use.
     *
     * @param ctx the application context
     */
    public void prepare(@NonNull final Context ctx) {
        this.mContext = ctx;
        final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionResult = apiAvailability.isGooglePlayServicesAvailable(ctx);

        if (connectionResult == ConnectionResult.SUCCESS) {
            activityRecognitionClient = ActivityRecognition.getClient(ctx);
            pendingIntent = PendingIntent.getService(ctx, MotionIntentService.REQUEST_CODE,
                    new Intent(ctx, MotionIntentService.class),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            try {

                ProviderInstaller.installIfNeededAsync(ctx, new ProviderInstaller.ProviderInstallListener() {
                    @Override
                    public void onProviderInstalled() {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "prepare: play services onProviderInstalled");
                        }
                    }

                    @Override
                    public void onProviderInstallFailed(final int errorCode, final Intent intent) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "prepare: play services onProviderInstallFailed");
                        }

                        if (apiAvailability.isUserResolvableError(errorCode)) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "prepare: play services onProviderInstallFailed");
                            }

                            apiAvailability.showErrorNotification(ctx, errorCode);

                        } else {
                            // TODO - unrecoverable
                        }
                    }
                });
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "prepare: play services unavailable");
                    e.printStackTrace();
                }
            }
        } else {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "prepare: play services unavailable");
            }
            apiAvailability.showErrorNotification(ctx, connectionResult);
        }
    }

    public void connect() {
        if (activityRecognitionClient != null && pendingIntent != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || PermissionChecker.checkSelfPermission(mContext, Manifest.permission.ACTIVITY_RECOGNITION) == PermissionChecker.PERMISSION_GRANTED) {
                // List of activity transitions to track
                final List<ActivityTransition> transitions = new ArrayList<>();
                transitions.add(new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
                transitions.add(new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
                transitions.add(new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
                transitions.add(new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_FOOT)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
                transitions.add(new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
                transitions.add(new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
                activityRecognitionClient.requestActivityTransitionUpdates(new ActivityTransitionRequest(transitions), pendingIntent);
            } else if (DEBUG) {
                MyLog.i(CLS_NAME, "onConnected: no permission");
            }
        }
    }

    @Override
    public void onResult(@NonNull final Status status) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "onResult");
        }
    }

    /**
     * Cancel any future callbacks and disconnect the client.
     */
    public void destroy() {
        if (activityRecognitionClient != null && pendingIntent != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && PermissionChecker.checkSelfPermission(mContext, Manifest.permission.ACTIVITY_RECOGNITION) != PermissionChecker.PERMISSION_GRANTED) {
                    return;
                }
                activityRecognitionClient.removeActivityTransitionUpdates(pendingIntent).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        pendingIntent.cancel();
                    }
                });
            } catch (final Exception e) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "destroy: Exception");
                    e.printStackTrace();
                }
            }
        }
    }
}
