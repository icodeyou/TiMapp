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
import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.activities.SplashActivity;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.server.ServerNotifications;
import com.timappweb.timapp.utils.NotificationFactory;

import java.util.HashMap;
import java.util.Map;

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

        try {
            String from = message.getFrom();
            Log.d(TAG, "Message received from: " + from);
            // Check if message contains a data payload.
            Intent intent = null;

            if (message.getData().size() > 0) {
                intent = this.parseData(message.getData());
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
                if (intent == null) {
                    intent = new Intent(this, SplashActivity.class);
                }
                sendNotification(notification, intent);
            }
        }
        catch (Exception ex){
            Log.e(TAG, "Exception while parsing notification: " + ex.getMessage());
            if (BuildConfig.DEBUG){
                ex.printStackTrace();
            }
        }
    }

    private Intent parseData(Map<String, String> data) {
        Log.d(TAG, "Message data payload: " + data);
        if (data.containsKey(ServerNotifications.KEY_EVENT_ID)){
            long eventId = Long.parseLong(data.get(ServerNotifications.KEY_EVENT_ID));
            return IntentsUtils.buildIntentViewPlace(this, eventId);
        }
        return null;
    }


    /**
     * Create and show a simple notification containing the received GCM message.
     *
     */
    private int sendNotification(Notification notification, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        Context context = MyApplication.getApplicationBaseContext();
        return NotificationFactory.build(context,
                R.mipmap.ic_launcher, // TODO JACK set icon timapp notification
                notification.getTitle() != null ? notification.getTitle() : context.getString(R.string.app_name),
                notification.getBody(),
                intent);
    }

}
