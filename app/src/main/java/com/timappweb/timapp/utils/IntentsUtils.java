package com.timappweb.timapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.activities.AddPlaceActivity;
import com.timappweb.timapp.activities.ExploreActivity;
import com.timappweb.timapp.activities.FilterActivity;
import com.timappweb.timapp.activities.LocateActivity;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.activities.PostActivity;
import com.timappweb.timapp.activities.ProfileActivity;
import com.timappweb.timapp.activities.TagActivity;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;

public class IntentsUtils {


    public static void login(Context context){
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    public static void profile(Activity activity){
        Intent intent = new Intent(activity, ProfileActivity.class);
        activity.startActivity(intent);
    }

    public static void home(Activity activity) {
        Intent intent = new Intent(activity, ExploreActivity.class);
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

    public static void viewPost(Activity activity, Post post) {
        Intent intent = new Intent(activity, PostActivity.class);
        intent.putExtra("post", post);          // TODO use constant
        activity.startActivity(intent);
    }
    public static void viewPost(Context activity, int postId) {
        Intent intent = new Intent(activity, PostActivity.class);
        intent.putExtra("post.id", postId);          // TODO use constant
        activity.startActivity(intent);
    }

    public static void addPost(Context context, Place place) {
        Intent intent = new Intent(context, TagActivity.class);
        intent.putExtra("place", place);          // TODO use constant
        context.startActivity(intent);
    }

    public static void viewPlace(Activity activity) {
        Intent intent = new Intent(activity, PlaceActivity.class);
        activity.startActivity(intent);
    }

    public static void addPlace(Context activity) {
        Intent intent = new Intent(activity, AddPlaceActivity.class);
        activity.startActivity(intent);
    }
    /**
     * Redirect to the last activity we attempt to go
     * before being redirected to the login activity
     * TODO implement
     */
    public static void lastActivityBeforeLogin(Activity activity) {
        home(activity);
    }
}
