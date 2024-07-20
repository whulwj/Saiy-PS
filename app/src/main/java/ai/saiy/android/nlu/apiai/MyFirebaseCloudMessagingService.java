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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ai.saiy.android.R;
import ai.saiy.android.service.NotificationService;
import ai.saiy.android.ui.activity.ActivityHome;
import ai.saiy.android.ui.notification.NotificationHelper;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsAuth;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 */
public class MyFirebaseCloudMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseCloudMessagingService.class.getSimpleName();
    public static final int REQUEST_CODE = 16;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        MyLog.i(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            MyLog.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String notificationBody = remoteMessage.getNotification().getBody();
            if (notificationBody != null) {
                sendNotification(notificationBody);
            }
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        final String oldToken = UtilsAuth.token;
        UtilsAuth.token = token;
        if (TextUtils.equals(oldToken, token)) {
            MyLog.w(TAG, "onNewToken already get");
            return;
        }
        UtilsAuth.getFirebaseInstanceToken();
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        final Intent intent = new Intent(this, ActivityHome.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent,
                PendingIntent.FLAG_IMMUTABLE);

        final String channelId = NotificationHelper.NOTIFICATION_CHANNEL_INFORMATION;
        final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(channelId,
                    getString(R.string.menu_not_channel_information),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(NotificationService.NOTIFICATION_FCM, notificationBuilder.build());
    }
}
