package com.timappweb.timapp.config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.desmond.squarecamera.CameraActivity;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddEventActivity;
import com.timappweb.timapp.activities.AddSpotActivity;
import com.timappweb.timapp.activities.AddTagActivity;
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
import com.timappweb.timapp.activities.EventPicturesActivity;
import com.timappweb.timapp.activities.PostActivity;
import com.timappweb.timapp.activities.ProfileActivity;
import com.timappweb.timapp.activities.SettingsActivity;
import com.timappweb.timapp.activities.ShareActivity;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventPost;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.utils.SerializeHelper;
import com.timappweb.timapp.utils.location.LocationManager;

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

    public static final int ACTION_ADD_EVENT_PICTURE = 4;

    public static final String KEY_ACTION = "action";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER = "user";
    public static final String VIEW_PICTURE_POSITION = "position";
    public static final String VIEW_PICTURE_LIST = "pictures";
    private static final String KEY_EVENT = "event";
    public static final String KEY_SPOT = "spot";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TITLE = "title";

    public static void login(Context context){
        login(context, true);
    }
    public static void login(Context context, boolean clear){
        Intent intent = new Intent(context, LoginActivity.class);
        if (clear)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void share(Activity activity){
        if (!requireLogin(activity, false))
            return;
        Intent intent = new Intent(activity, ShareActivity.class);
        activity.startActivity(intent);
    }

    public static void home(Context context) {
        Intent intent = new Intent(context, DrawerActivity.class);
        context.startActivity(intent);
    }
    public static void fatalError(Context context, int idTitle, int idMessage) {
        Intent intent = new Intent(context, ErrorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(IntentsUtils.KEY_TITLE, context.getResources().getString(idTitle));
        intent.putExtra(IntentsUtils.KEY_MESSAGE, context.getResources().getString(idMessage));
        context.startActivity(intent);
    }
    public static void error(Context context, int idTitle, int idMessage) {
        IntentsUtils.fatalError(context, idTitle, idMessage);
    }

    public static void profile(Context context){
        if (!requireLogin(context, false))
            return;
        Intent intent = new Intent(context, ProfileActivity.class);
        context.startActivity(intent);
    }

    public static void profile(Context context, User user) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(KEY_USER_ID, user.remote_id);
        Log.d(TAG, "Intent to view profile: " + user.remote_id);
        context.startActivity(intent);
    }

    public static void profile(User user) {
        Context context = MyApplication.getApplicationBaseContext();
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(KEY_USER_ID, user.remote_id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d(TAG, "Intent to view profile: " + user.remote_id);
        context.startActivity(intent);
    }


    public static void editProfile(ProfileActivity activity, User user) {
        if (!requireLogin(activity, false))
            return;

        Intent intent = new Intent(activity, EditProfileActivity.class);
        intent.putExtra(KEY_USER, user);
        activity.startActivityForResult(intent, ProfileActivity.ACTIVITY_RESULT_EDIT_PROFILE);
    }

    public static void logout(Activity activity) {
        if (!requireLogin(activity, true))
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

    public static void viewPost(Activity activity, EventPost eventPost) {
        Intent intent = new Intent(activity, PostActivity.class);
        intent.putExtra("eventPost", eventPost);          // TODO use constant
        activity.startActivity(intent);
    }
    public static void viewPost(Context activity, int postId) {
        Intent intent = new Intent(activity, PostActivity.class);
        intent.putExtra("eventPost.id", postId);          // TODO use constant
        activity.startActivity(intent);
    }


    public static void locate(Context context) {
        if (!requireLogin(context, false))
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
        if (!requireLogin(activity, false))
            return;
        Intent startCustomCameraIntent = new Intent(activity, CameraActivity.class);
        activity.startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
    }

    public static void addPictureFromFragment(Context context, Fragment fragment) {
        if (!requireLogin(context, false))
            return;
        Intent startCustomCameraIntent = new Intent(context, CameraActivity.class);
        fragment.startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
    }

    public static void addTags(Activity activity, Event event) {
        if (!requireLogin(activity, false))
            return;

        Intent intent = new Intent(activity, AddTagActivity.class);
        Bundle extras = new Bundle();
        extras.putString(IntentsUtils.KEY_EVENT, SerializeHelper.pack(event));
        intent.putExtras(extras);
        activity.startActivityForResult(intent, REQUEST_TAGS);
    }

    public static void inviteFriendToEvent(Activity activity, Event event) {
        Intent intent = new Intent(activity, InviteFriendsActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable(IntentsUtils.KEY_EVENT, SerializeHelper.pack(event));
        intent.putExtras(extras);
        activity.startActivityForResult(intent, REQUEST_INVITE_FRIENDS);
    }

    public static void viewEventFromId(Activity activity, int id) {
        activity.finish();
        Intent intent = buildIntentViewPlace(activity, id);
        activity.startActivity(intent);
    }

    public static void viewSpecifiedEvent(Context context, Event event) {
        context.startActivity(buildIntentViewPlace(context, event));
    }


    public static void postEvent(Context context, Event event, int action) {
        if (context instanceof EventActivity){
            ((EventActivity)context).parseActionParameter(action);
        }
        else {
            Intent intent = buildIntentViewPlace(context, event);
            intent.putExtra(KEY_ACTION, action);
            context.startActivity(intent);
        }
    }

    public static Intent buildIntentViewPlace(Context context, int placeId) {
        Intent intent = new Intent(context, EventActivity.class);
        intent.putExtra("place_id", placeId);
        return  intent;
    }
    public static Intent buildIntentViewPlace(Context context, Event event) {
        Intent intent = new Intent(context, EventActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable(IntentsUtils.KEY_EVENT, SerializeHelper.pack(event));          // TODO use constant
        intent.putExtras(extras);
        return intent;
    }

    public static void viewPicture(Activity activity, int position, String[] data) {
        Intent intent = new Intent(activity, EventPicturesActivity.class);
        intent.putExtra(VIEW_PICTURE_POSITION, position);
        intent.putExtra(VIEW_PICTURE_LIST, data);
        activity.startActivity(intent);
    }

    public static void addPlace(Activity activity) {
        if (!requireLogin(activity, false))
            return;
        if (!QuotaManager.instance().checkQuota(QuotaType.PLACES, true)){
            //Toast.makeText(context, R.string.create_second_place_delay, Toast.LENGTH_LONG).show();
            return;
        }
        activity.finish();
        Intent intent = new Intent(activity, AddEventActivity.class);
        activity.startActivity(intent);
    }
/*
    public static void comment(Activity activity, String comment) {
        Intent intent = new Intent(activity, DescriptionActivity.class);
        intent.putExtra("comment", comment);
        activity.startActivityForResult(intent, REQUEST_COMMENT);
    }*/

    public static String extractComment(Intent intent) {
        if(intent.getExtras()!=null) {
            return intent.getExtras().getString("comment");
        } else {
            return null;
        }
    }

    public static void pinSpot(Activity activity) {
        IntentsUtils.pinSpot(activity, null);
    }

    public static void pinSpot(Activity activity, Spot spot) {
        if (!requireLogin(activity, false))
            return;
        if (!QuotaManager.instance().checkQuota(QuotaType.PLACES, true)){
            //Toast.makeText(context, R.string.create_second_place_delay, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(activity, AddSpotActivity.class);
        intent.putExtra(IntentsUtils.KEY_SPOT, spot);
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
        if (!requireLogin(context, false))
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

    public static Event extractEvent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null){
            return null;
        }
        return SerializeHelper.unpack(extras.getString(IntentsUtils.KEY_EVENT), Event.class);
    }

    public static String[] extractPicture(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null){
            return null;
        }
        return extras.getStringArray(IntentsUtils.VIEW_PICTURE_LIST);
    }

    public static EventPost extractPost(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null){
            return null;
        }
        return (EventPost) extras.getSerializable("eventPost");
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


    public static boolean requireLogin(Context context, boolean clear){
        if (!MyApplication.isLoggedIn()){
            Toast.makeText(context, R.string.error_require_login, Toast.LENGTH_LONG).show();
            IntentsUtils.login(context, clear);
            return false;
        }
        return true;
    }

    public static boolean requireUpToDateLocation(Context context, boolean clear) {
        if (!LocationManager.hasUpToDateLastLocation()){
            Toast.makeText(context, R.string.error_require_gps, Toast.LENGTH_LONG).show();
            IntentsUtils.login(context, clear);
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

    public static Spot extractSpot(Intent intent) {
        Bundle extras = intent.getExtras();
        return extras != null ? (Spot) extras.getSerializable(IntentsUtils.KEY_SPOT) : null;
    }

    public static void exitDescriptionActivity(Activity activity, String description) {
        Intent intent = new Intent(activity, AddEventActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("description", description);
        intent.putExtras(bundle);
        activity.setResult(Activity.RESULT_OK, intent);
    }

}