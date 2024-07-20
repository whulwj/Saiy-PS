/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.saiy.android.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;

import java.util.concurrent.TimeUnit;

public class UtilsAuth {
    private static final String TAG = UtilsAuth.class.getSimpleName();
    private static String firebaseInstanceId = "";
    /** A new FIS Auth-Token, created for this Firebase Installation. */
    public static String token = "";
    /**
     * The amount of time, in seconds, before the auth-token expires for this Firebase Installation.
     */
    public static long expiryTime;

    /**
     * <a href="https://medium.com/@akkic2229/handling-firebase-cloud-messagings-expired-token-cleanup-90552e9d2656" />
     * function to manually trigger a token refresh
     */
    public static void refreshFirebaseInstanceToken() {
        FirebaseInstallations.getInstance().getToken(true).addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<InstallationTokenResult> task) {
                if (task.isSuccessful()) {
                    final InstallationTokenResult result = task.getResult();
                    if (result != null) {
                        // Token refresh successful
                        final String deviceToken = result.getToken();
                        token = deviceToken;
                        expiryTime = result.getTokenExpirationTimestamp();
                        MyLog.i(TAG, deviceToken);
                    } else {
                        MyLog.w(TAG, "refresh auth token failed");
                    }
                } else {
                    // Handle the error
                    MyLog.w(TAG, "refresh auth token failed:" + task.getException());
                }
            }
        });
    }

    /**
     * function to get the FirebaseInstanceId token
     */
    public static void getFirebaseInstanceId() {
        if (TextUtils.isEmpty(firebaseInstanceId)) {
            FirebaseInstallations.getInstance().getId()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (task.isSuccessful()) {
                                firebaseInstanceId = task.getResult();
                                MyLog.d(TAG, "Installation ID: " + task.getResult());
                            } else {
                                MyLog.e(TAG, "Unable to get Installation ID");
                            }
                            getFirebaseInstanceToken();
                        }
                    });
        } else {
            getFirebaseInstanceToken();
        }
    }

    public static void getFirebaseInstanceToken() {
        FirebaseInstallations.getInstance().getToken(false).addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<InstallationTokenResult> task) {
                if (task.isSuccessful()) {
                    final InstallationTokenResult result = task.getResult();
                    if (result != null) {
                        // Get new FCM registration token
                        final String deviceToken = result.getToken();
                        token = deviceToken;
                        expiryTime = result.getTokenExpirationTimestamp();
                        MyLog.i(TAG, deviceToken);
                    } else {
                        MyLog.w(TAG, "get auth token failed");
                    }
                } else {
                    // Handle the error
                    MyLog.w(TAG, "get auth token failed:" + task.getException());
                }
            }
        });
    }

    /**
     * function to check if the token is valid
     * @return  boolean :   indicates the status of the signin
     */
    public static boolean isTokenValid() {
        return UtilsAuth.expiryTime != 0 && !TextUtils.isEmpty(UtilsAuth.token)
                && UtilsAuth.expiryTime > TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    }

}
