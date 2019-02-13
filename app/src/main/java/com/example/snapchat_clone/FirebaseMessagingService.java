package com.example.snapchat_clone;

import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String messageTitle = remoteMessage.getNotification().getTitle();
        String messageBody = remoteMessage.getNotification().getBody();

        Notification.Builder noti = new Notification.Builder(this, getString(R.string.default_notification_channel_id))
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setSmallIcon(R.mipmap.ghostnotification)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ghostnotification));

        // set a random id for the notification
        int mNotificationId = (int) System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationId, noti.build());


    }
}
