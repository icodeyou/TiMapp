package com.timappweb.timapp.services;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.RemoteMessage.Notification;
import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.SplashActivity;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.server.ServerNotifications;
import com.timappweb.timapp.utils.KeyValueStorage;
import com.timappweb.timapp.utils.NotificationFactory;

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
        if (data.containsKey(ServerNotifications.KEY_NOTIFICATION_TYPE)){
            String type = data.get(ServerNotifications.KEY_NOTIFICATION_TYPE);
            switch (type){
                case ServerNotifications.TYPE_OPEN_EVENT:
                case ServerNotifications.TYPE_EVENT_INVITE:
                    if (data.containsKey(ServerNotifications.KEY_EVENT_ID)){
                        long eventId = Long.parseLong(data.get(ServerNotifications.KEY_EVENT_ID));
                        return IntentsUtils.buildIntentViewEvent(this, eventId);
                    }
                    break;
                case ServerNotifications.TYPE_REQUIRE_UPDATE:
                    ConfigurationProvider.rules().should_update = true;
                    ConfigurationProvider.saveRules();
                    KeyValueStorage.in().remove(SplashActivity.KEY_SHOULD_UPDATE_DIALOG).commit();
                    return new Intent(this, SplashActivity.class);
                default:
                    Log.e(TAG, "Invalid message, no notification type: " + type);
                    return null;
            }
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
                R.drawable.logo_transparent,
                notification.getTitle() != null ? notification.getTitle() : context.getString(R.string.app_name),
                notification.getBody(),
                intent);
    }

}
