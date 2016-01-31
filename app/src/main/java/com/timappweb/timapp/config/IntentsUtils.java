package com.timappweb.timapp.config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.timappweb.timapp.cache.CacheData;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddPlaceActivity;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.activities.EditProfileActivity;
import com.timappweb.timapp.activities.FilterActivity;
import com.timappweb.timapp.activities.LocateActivity;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.activities.PostActivity;
import com.timappweb.timapp.activities.ProfileActivity;
import com.timappweb.timapp.activities.PublishActivity;
import com.timappweb.timapp.activities.SettingsActivity;
import com.timappweb.timapp.activities.ShareActivity;
import com.timappweb.timapp.activities.TagActivity;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;

import java.util.List;

public class IntentsUtils {


    public static void login(Context context){
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    public static void share(Activity activity){
        if (!requireLogin(activity))
            return;
        Intent intent = new Intent(activity, ShareActivity.class);
        activity.startActivity(intent);
    }

    public static void home(Activity activity) {
        Intent intent = new Intent(activity, DrawerActivity.class);
        activity.startActivity(intent);
    }

    public static void profile(Activity activity){
        if (!requireLogin(activity))
            return;
        Intent intent = new Intent(activity, ProfileActivity.class);
        activity.startActivity(intent);
    }

    public static void profile(Activity activity, String username) {
        if (!requireLogin(activity))
            return;
        Intent intent = new Intent(activity, ProfileActivity.class);
        intent.putExtra("username", username); // TODO use constant
        activity.startActivity(intent);
    }


    public static void editProfile(Activity activity) {
        if (!requireLogin(activity))
            return;
        Intent intent = new Intent(activity, EditProfileActivity.class);
        activity.startActivity(intent);
    }

    public static void logout(Activity activity) {
        if (!requireLogin(activity))
            return;
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

    public static void addPostStepTags(Context context, Place place, Post post) {
        if (!requireLogin(context))
            return;
        if (!CacheData.isAllowedToAddPost()){
            Toast.makeText(context, R.string.create_second_post_delay, Toast.LENGTH_LONG);
            return;
        }
        Intent intent = new Intent(context, TagActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("place", place);          // TODO use constant
        extras.putSerializable("post", post);          // TODO use constant
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    public static void addPostStepPublish(Context context, Place place, Post post) {
        if (!requireLogin(context))
            return;
        Intent intent = new Intent(context, PublishActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("place", place);          // TODO use constant
        extras.putSerializable("post", post);          // TODO use constant
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    public static void addPostStepLocate(Context context) {
        if (!requireLogin(context))
            return;
        Intent intent = new Intent(context, LocateActivity.class);
        context.startActivity(intent);
    }

    public static void viewPlaceFromPublish(Context context, int id) {
        Intent intent = new Intent(context, PlaceActivity.class);
        intent.putExtra("place_id", id);
        context.startActivity(intent);
    }

    public static void viewPlaceFromMap(Context context, Place place) {
        Intent intent = new Intent(context, PlaceActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("place", place);          // TODO use constant
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    public static void addPlace(Context context) {
        if (!requireLogin(context))
            return;
        if (!CacheData.isAllowedToAddPlace()){
            Toast.makeText(context, R.string.create_second_place_delay, Toast.LENGTH_LONG);
            return;
        }
        Intent intent = new Intent(context, AddPlaceActivity.class);
        context.startActivity(intent);
    }
    /**
     * Redirect to the last activity we attempt to go
     * before being redirected to the login activity
     * TODO implement
     */
    public static void lastActivityBeforeLogin(Activity activity) {
        home(activity);
    }

    public static void settings(Context context) {
        if (!requireLogin(context))
            return;
        Intent intent = new Intent(context,SettingsActivity.class);
        context.startActivity(intent);
    }

    public static void reload(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(0, 0);
        Intent intent = activity.getIntent();
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    public static Place extractPlace(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null){
            return null;
        }
        return (Place) extras.getSerializable("place");
    }
    public static Post extractPost(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null){
            return null;
        }
        return (Post) extras.getSerializable("post");
    }

    public static int extractUserId(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null){
            return -1;
        }
        return extras.getInt("user_id", -1);
    }

    public static void addPostStepTags(Context context, Place place) {
        if (!requireLogin(context))
            return;
        Post post = new Post();
        post.latitude = MyApplication.getLastLocation().getLatitude();
        post.longitude = MyApplication.getLastLocation().getLongitude();
        IntentsUtils.addPostStepTags(context, place, post);
    }

    public static boolean requireLogin(Context context){
        if (!MyApplication.isLoggedIn()){
            IntentsUtils.login(context);
            return false;
        }
        return true;
    }

    public static int extractPlaceId(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null){
            return -1;
        }
        return extras.getInt("place_id", -1);
    }
}
