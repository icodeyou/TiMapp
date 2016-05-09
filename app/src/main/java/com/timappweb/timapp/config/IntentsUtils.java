package com.timappweb.timapp.config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.desmond.squarecamera.CameraActivity;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.activities.AddPlaceActivity;
import com.timappweb.timapp.activities.AddSpotActivity;
import com.timappweb.timapp.activities.DescriptionActivity;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.activities.EditProfileActivity;
import com.timappweb.timapp.activities.ErrorActivity;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.activities.FilterActivity;
import com.timappweb.timapp.activities.InvitationsActivity;
import com.timappweb.timapp.activities.InviteFriendsActivity;
import com.timappweb.timapp.activities.ListFriendsActivity;
import com.timappweb.timapp.activities.LocateActivity;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.activities.PlaceViewPagerActivity;
import com.timappweb.timapp.activities.PostActivity;
import com.timappweb.timapp.activities.ProfileActivity;
import com.timappweb.timapp.activities.PublishActivity;
import com.timappweb.timapp.activities.SettingsActivity;
import com.timappweb.timapp.activities.ShareActivity;
import com.timappweb.timapp.activities.TagActivity;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.Post;
import com.timappweb.timapp.data.models.User;

public class IntentsUtils {

    private static final String TAG = "IntentUtils";

    public static final int     REQUEST_PICK_SPOT = 1;
    public static final int     REQUEST_INVITE_FRIENDS = 1;
    public static final int     REQUEST_COMMENT = 2;
    public static final int     REQUEST_CAMERA = 3;
    public static final int     REQUEST_TAGS = 4;
    public static final int     REQUEST_PUBLISH = 5;

    public static final int ACTION_CAMERA = 1;
    public static final int ACTION_TAGS = 2;
    public static final int ACTION_PEOPLE = 3;

    public static final String KEY_ACTION = "action";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER = "user";

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

    public static void profile(Context context){
        if (!requireLogin(context))
            return;
        Intent intent = new Intent(context, ProfileActivity.class);
        context.startActivity(intent);
    }

    public static void profile(Activity activity, User user) {
        Intent intent = new Intent(activity, ProfileActivity.class);
        intent.putExtra(KEY_USER_ID, user.remote_id);
        Log.d(TAG, "Intent to view profile: " + user.remote_id);
        activity.startActivity(intent);
    }


    public static void editProfile(ProfileActivity activity, User user) {
        if (!requireLogin(activity))
            return;

        Intent intent = new Intent(activity, EditProfileActivity.class);
        intent.putExtra(KEY_USER, user);
        activity.startActivityForResult(intent, ProfileActivity.ACTIVITY_RESULT_EDIT_PROFILE);
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

    public static void publishPage(Activity activity, Place place, Post post) {
        if (!requireLogin(activity))
            return;
        Intent intent = new Intent(activity, PublishActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("place", place);          // TODO use constant
        extras.putSerializable("post", post);          // TODO use constant
        intent.putExtras(extras);
        activity.startActivityForResult(intent, REQUEST_PUBLISH);
    }

    public static void locate(Context context) {
        if (!requireLogin(context))
            return;
        if (!QuotaManager.instance().checkQuota(QuotaType.ADD_POST, true)){
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

    public static void addPicture(Activity activity) {
        Intent startCustomCameraIntent = new Intent(activity, CameraActivity.class);
        activity.startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
    }

    public static void addPictureFromFragment(Context context, Fragment fragment) {
        Intent startCustomCameraIntent = new Intent(context, CameraActivity.class);
        fragment.startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
    }

    public static void addTags(Activity activity, Place place, Post post) {
        if (!requireLogin(activity))
            return;

        Intent intent = new Intent(activity, TagActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("place", place);          // TODO use constant
        extras.putSerializable("post", post);          // TODO use constant
        intent.putExtras(extras);
        activity.startActivityForResult(intent, REQUEST_TAGS);
/*
        //TRY TRANSITION ... FAIL
        // Following the documentation, right after starting the activity
        // we override the transition
        UserActivity activity  = (UserActivity) context;
        activity.overridePendingTransition(R.anim.in_from_left, R.anim.in_from_left);*/
    }

    public static void addPeople(Activity activity, Place place) {
        Intent intent = new Intent(activity, InviteFriendsActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("place", place);          // TODO use constant
        intent.putExtras(extras);
        activity.startActivityForResult(intent, REQUEST_INVITE_FRIENDS);
    }

    public static void viewEventFromId(Context context, int id) {
        Intent intent = buildIntentViewPlace(context, id);
        context.startActivity(intent);
    }

    public static void viewSpecifiedEvent(Context context, Place place) {
        context.startActivity(buildIntentViewPlace(context, place));
    }

    public static void postOutsideEvent(Context context, Place place, int action) {
        Intent intent = buildIntentViewPlace(context, place);
        intent.putExtra(KEY_ACTION, action);
        context.startActivity(intent);
    }

    public static void postInsideEvent(EventActivity eventActivity, int action) {
        Intent intent = new Intent();
        intent.putExtra(KEY_ACTION, action);
        eventActivity.setActions();
    }

    public static Intent buildIntentViewPlace(Context context, int placeId) {
        Intent intent = new Intent(context, EventActivity.class);
        intent.putExtra("place_id", placeId);
        return  intent;
    }
    public static Intent buildIntentViewPlace(Context context, Place place) {
        Intent intent = new Intent(context, EventActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("place", place);          // TODO use constant
        intent.putExtras(extras);
        return intent;
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
        if (!QuotaManager.instance().checkQuota(QuotaType.PLACES, true)){
            //Toast.makeText(context, R.string.create_second_place_delay, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(context, AddPlaceActivity.class);
        context.startActivity(intent);
    }

    public static void comment(Activity activity) {
        Intent intent = new Intent(activity, DescriptionActivity.class);
        activity.startActivityForResult(intent, REQUEST_COMMENT);
    }

    public static void pinSpot(Activity activity) {
        if (!requireLogin(activity))
            return;
        if (!QuotaManager.instance().checkQuota(QuotaType.PLACES, true)){
            //Toast.makeText(context, R.string.create_second_place_delay, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(activity, AddSpotActivity.class);
        activity.startActivityForResult(intent, REQUEST_PICK_SPOT);
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
        return (User) extras.getSerializable(KEY_USER);
    }

    public static int extractUserId(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null){
            Log.e(TAG, "There is no extra");
            return -1;
        }
        return extras.getInt(KEY_USER_ID, -1);
    }

    public static void addTags(Activity activity, Place place) {
        if (!requireLogin(activity))
            return;
        if (!QuotaManager.instance().checkQuota(QuotaType.ADD_POST, true)){
            //Toast.makeText(context, R.string.create_second_post_delay, Toast.LENGTH_LONG).show();
            return;
        }
        Post post = new Post();
        post.latitude = MyApplication.getLastLocation().getLatitude();
        post.longitude = MyApplication.getLastLocation().getLongitude();
        IntentsUtils.addTags(activity, place, post);
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