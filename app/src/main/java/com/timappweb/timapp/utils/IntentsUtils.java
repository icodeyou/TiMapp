package com.timappweb.timapp.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.activities.FilterActivity;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.activities.PostActivity;
import com.timappweb.timapp.activities.ProfileActivity;
import com.timappweb.timapp.entities.Post;

/**
 * Created by stephane on 12/14/2015.
 */
public class IntentsUtils {

    public static void login(Activity activity){
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
    }

    public static void profile(Activity activity){
        Intent intent = new Intent(activity, ProfileActivity.class);
        activity.startActivity(intent);
    }

    public static void home(Activity activity) {
        Intent intent = new Intent(activity, DrawerActivity.class);
        activity.startActivity(intent);
    }

    public static void profile(Activity activity, String username) {
        Intent intent = new Intent(activity, ProfileActivity.class);
        intent.putExtra("username", username); // TODO use constant
        activity.startActivity(intent);
    }

    public static void logout(Activity activity) {
        MyApplication.logout();
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
    }

    public static void filter(Activity activity) {
        Intent intent = new Intent(activity, FilterActivity.class);
        activity.startActivity(intent);
    }

    public static void post(Activity activity, Post post) {
        Intent intent = new Intent(activity, PostActivity.class);
        intent.putExtra("post", post);          // TODO use constant
        activity.startActivity(intent);
    }
}
