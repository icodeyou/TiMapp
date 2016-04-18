package com.timappweb.timapp.config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.timappweb.timapp.activities.AddSpotActivity;
import com.timappweb.timapp.activities.ErrorActivity;
import com.timappweb.timapp.activities.InvitationsActivity;
import com.timappweb.timapp.activities.ListFriendsActivity;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.activities.AddPlaceActivity;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.activities.EditProfileActivity;
import com.timappweb.timapp.activities.FilterActivity;
import com.timappweb.timapp.activities.InviteFriendsActivity;
import com.timappweb.timapp.activities.LocateActivity;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.activities.PostActivity;
import com.timappweb.timapp.activities.ProfileActivity;
import com.timappweb.timapp.activities.PublishActivity;
import com.timappweb.timapp.activities.SettingsActivity;
import com.timappweb.timapp.activities.ShareActivity;
import com.timappweb.timapp.activities.TagActivity;
import com.timappweb.timapp.activities.PlaceViewPagerActivity;
import com.timappweb.timapp.database.models.QuotaType;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.User;

public class IntentsUtils {


    private static final String TAG = "IntentUtils";

    public static void login(Context context){
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
    public static void serverError(Context context) {
        Intent intent = new Intent(context, ErrorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    public static void error(Context context) {
        IntentsUtils.serverError(context);
    }

    public static void profile(Activity activity){
        if (!requireLogin(activity))
            return;
        Intent intent = new Intent(activity, ProfileActivity.class);
        activity.startActivity(intent);
    }

    public static void profile(Activity activity, User user) {
        Intent intent = new Intent(activity, ProfileActivity.class);
        intent.putExtra("user_id", user.id);
        Log.d(TAG, "Intent to view profile: " + user.id);
        activity.startActivity(intent);
    }


    public static void editProfile(Activity activity, User user) {
        if (!requireLogin(activity))
            return;

        Intent intent = new Intent(activity, EditProfileActivity.class);
        intent.putExtra("user", user);
        activity.startActivity(intent);
    }

    public static void logout(Activity activity) {
        if (!requireLogin(activity))
            return;
        MyApplication.logout();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

        Intent intent = new Intent(context, TagActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("place", place);          // TODO use constant
        extras.putSerializable("post", post);          // TODO use constant
        intent.putExtras(extras);
        context.startActivity(intent);
/*
        //TRY TRANSITION ... FAIL
        // Following the documentation, right after starting the activity
        // we override the transition
        UserActivity activity  = (UserActivity) context;
        activity.overridePendingTransition(R.anim.in_from_left, R.anim.in_from_left);*/
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
        if (!QuotaManager.instance().checkQuota(QuotaType.POST, true)){
            //Toast.makeText(context, R.string.create_second_place_delay, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, LocateActivity.class);
        context.startActivity(intent);
    }

    public static void listFriends(Context context) {
        Intent intent = new Intent(context, ListFriendsActivity.class);
        context.startActivity(intent);
    }

    public static void invitations(Context context) {
        Intent intent = new Intent(context, InvitationsActivity.class);
        context.startActivity(intent);
    }

    public static void viewPlaceFromPublish(Activity activity, int id) {
        Intent intent = buildIntentViewPlace(activity, id);
        activity.startActivity(intent);
        activity.finish();
    }
    public static Intent buildIntentViewPlace(Context context, int placeId) {
        Intent intent = new Intent(context, PlaceActivity.class);
        intent.putExtra("place_id", placeId);
        return  intent;
    }
    public static Intent buildIntentViewPlace(Context context, Place place) {
        Intent intent = new Intent(context, PlaceActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("place", place);          // TODO use constant
        intent.putExtras(extras);
        return intent;
    }

    public static void viewSpecifiedPlace(Context context, Place place) {
        context.startActivity(buildIntentViewPlace(context, place));
    }

    public static void addPeople(Activity activity, Place place) {
        Intent intent = new Intent(activity, InviteFriendsActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("place", place);          // TODO use constant
        intent.putExtras(extras);
        activity.startActivity(intent);
    }

    public static void viewPicture(Activity activity, int position, String[] data) {
        Intent intent = new Intent(activity, PlaceViewPagerActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("pictures", data);
        activity.startActivity(intent);
    }

    public static void addPlace(Context context) {
        if (!requireLogin(context))
            return;
        if (!QuotaManager.instance().checkQuota(QuotaType.PLACE, true)){
            //Toast.makeText(context, R.string.create_second_place_delay, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, AddPlaceActivity.class);
        context.startActivity(intent);
    }

    public static void pinSpot(Context context) {
        if (!requireLogin(context))
            return;
        if (!QuotaManager.instance().checkQuota(QuotaType.PLACE, true)){
            //Toast.makeText(context, R.string.create_second_place_delay, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, AddSpotActivity.class);
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

    public static String[] extractPicture(Intent intent) {
        Bundle extras = intent.getExtras();
        //TODO Steph : remove condition :
        if (extras == null){
            return null;
        }
        return extras.getStringArray("pictures");
    }

    public static Post extractPost(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null){
            return null;
        }
        return (Post) extras.getSerializable("post");
    }

    public static User extractUser(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null){
            return null;
        }
        return (User) extras.getSerializable("user");
    }

    public static int extractUserId(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null){
            Log.e(TAG, "There is no extra");
            return -1;
        }
        return extras.getInt("user_id", -1);
    }

    public static void addPostStepTags(Context context, Place place) {
        if (!requireLogin(context))
            return;
        if (!QuotaManager.instance().checkQuota(QuotaType.POST, true)){
            //Toast.makeText(context, R.string.create_second_post_delay, Toast.LENGTH_LONG).show();
            return;
        }
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
        return extras.getInt("place_id", Integer.valueOf(extras.getString("place_id", "-1"))); // usefull for notifications
    }

}
