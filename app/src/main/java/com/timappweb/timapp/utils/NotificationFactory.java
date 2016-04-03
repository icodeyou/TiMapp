package com.timappweb.timapp.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.User;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by stephane on 4/2/2016.
 */
public class NotificationFactory {

    private static final String TAG = "NotificationFactory";
    static int mId = 1;

    public static int build(Context context, int icon, String title, String text, Intent resultIntent){

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(icon)
                        .setContentTitle(title)
                        .setContentText(text);
// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(PlaceActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId++, mBuilder.build());
        return mId;
    }
    /**
     * Create a notification for a place
     * @param user
     * @param place
     */
    public static int invite(Context context, User user, Place place){
        Intent resultIntent = IntentsUtils.buildIntentViewPlace(context, place);
        return NotificationFactory.build(context, place.getIconResource(), user.username + " invited you!", "Event: " + place.name, resultIntent);
    }


    public static int invite(Context context, Bundle bundle) {
            Bundle notification = bundle.getBundle("notification");
            Bundle data = bundle.getBundle("data");
            if (notification == null || data == null){
                Log.e(TAG, "Received a null notification");
                return -1;
            }
            Log.v(TAG, ":notification " + notification);
            String body = notification.getString("body");
            String title = notification.getString("title");
            String icon = notification.getString("icon");

            int placeId = data.getInt("place_id");
            Intent resultIntent = IntentsUtils.buildIntentViewPlace(context, placeId);
            return NotificationFactory.build(context, R.drawable.ic_category_bar, title, body, resultIntent);
    }
}
