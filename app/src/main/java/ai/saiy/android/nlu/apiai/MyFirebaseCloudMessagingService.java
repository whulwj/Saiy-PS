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

package ai.saiy.android.nlu.apiai;

import ai.saiy.android.utils.AuthUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

public class MyFirebaseCloudMessagingService extends FirebaseMessagingService {
    public static final String TOKEN_RECEIVED = "TOKEN_RECEIVED";
    private static final String TAG = MyFirebaseCloudMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i("FirebaseMessage", "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.i(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        handleToken(token);
    }

    /**
     * function to save the token data in the AppController
     *
     * @param expiryTime :   expiry time received from FCM
     * @param token      :   token received from FCM
     */
    private void handleNotification(String expiryTime, String token) {
        AuthUtils.setExpiryTime(expiryTime);

        handleToken(token);
    }

    private void handleToken(String token) {
        AuthUtils.token = token;

        Intent intent = new Intent(TOKEN_RECEIVED);
        sendBroadcast(intent);
    }
}
