package com.timappweb.timapp.services;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.RemoteMessage.Notification;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.SplashActivity;
import com.timappweb.timapp.utils.NotificationFactory;

/**
 * Created by Stephane on 29/09/2016.
 */
public class MyFcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "MyGcmListenerService";

    /**
     *
     * @param message
     */
    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        String from = message.getFrom();
        Log.d(TAG, "Message received from: " + from);
        // Check if message contains a data payload.
        if (message.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + message.getData());
        }

        // Check if message contains a notification payload.
        if (message.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + message.getNotification().getBody());
        }
        Notification notification = message.getNotification();
        // Check if message contains a notification payload.
        if (notification != null) {
            Log.d(TAG, "Message Notification: " + notification);
            // Also if you intend on generating your own notifications as a result of a received FCM
            // message, here is where that should be initiated. See sendNotification method below.
            sendNotification(notification);
        }
    }


    /**
     * Create and show a simple notification containing the received GCM message.
     *
     */
    private void sendNotification(Notification notification) {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Context context = MyApplication.getApplicationBaseContext();
        int id = NotificationFactory.build(context,
                R.drawable.ic_cloud, // TODO JACK set icon timapp notification
                notification.getTitle() != null ? notification.getTitle() : context.getString(R.string.app_name),
                notification.getBody(),
                intent);
    }
}
