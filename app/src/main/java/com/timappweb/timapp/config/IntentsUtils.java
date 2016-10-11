package com.timappweb.timapp.config;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddEventActivity;
import com.timappweb.timapp.activities.AddSpotActivity;
import com.timappweb.timapp.activities.AddTagActivity;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.activities.EditProfileActivity;
import com.timappweb.timapp.activities.ErrorActivity;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.activities.EventPicturesActivity;
import com.timappweb.timapp.activities.FilterActivity;
import com.timappweb.timapp.activities.InvitationsActivity;
import com.timappweb.timapp.activities.InviteFriendsActivity;
import com.timappweb.timapp.activities.ListFriendsActivity;
import com.timappweb.timapp.activities.LocateActivity;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.activities.PresentationActivity;
import com.timappweb.timapp.activities.ProfileActivity;
import com.timappweb.timapp.activities.SettingsActivity;
import com.timappweb.timapp.activities.ShareActivity;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventPost;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.utils.SerializeHelper;
import com.timappweb.timapp.utils.location.LocationManager;

import pl.aprilapps.easyphotopicker.EasyImage;
import pl.tajchert.nammu.Nammu;
import pl.tajchert.nammu.PermissionCallback;

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
    public static final int ACTION_COMING= 4;

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
    public static final String KEY_EVENT_ID = "event_id";

    public static void login(Context context){
        login(context, true);
    }
    public static void login(Context context, boolean clear){
        Intent intent = new Intent(context, LoginActivity.class);
        if (clear)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void loginOrRedirectHome(Activity activity) {
        if(!MyApplication.isLoggedIn()) {
            login(activity);
        } else {
            if(activity.isTaskRoot()) {
                home(activity);
            } else {
                activity.finish();
            }
        }
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


    public static void locate(Context context) {
        if (!requireLogin(context, false))
            return;
        if (!LocationManager.getLocationProvider().isGPSEnabled()){
            Toast.makeText(context, R.string.ask_user_to_enable_gps, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!QuotaManager.instance().checkQuota(QuotaType.ADD_EVENT, true)){
            //Toast.makeText(context, R.string.create_second_place_delay, Toast.LENGTH_LONG).builder();
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

    public static void addPictureFromFragment(Context context, final Fragment fragment) {
        if (!requireLogin(context, false) || !QuotaManager.instance().checkQuota(QuotaType.ADD_PICTURE, true))
            return;

        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            EasyImage.openCamera(fragment, 0);
        } else {
            String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
            Nammu.askForPermission(fragment.getActivity(), permissions, new PermissionCallback() {
                @Override
                public void permissionGranted() {
                    EasyImage.openCamera(fragment, 0);
                }

                @Override
                public void permissionRefused() {

                }
            });
        }
    }

    public static void addPictureFromActivity(final Activity activity) {
        int permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            EasyImage.openCamera(activity, 0);
        } else {
            Nammu.askForPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, new PermissionCallback() {
                @Override
                public void permissionGranted() {
                    EasyImage.openCamera(activity, 0);
                }

                @Override
                public void permissionRefused() {

                }
            });
        }
    }

    public static void addTags(Activity activity, Event event) {
        if (!requireLogin(activity, false))
            return;
        if (!QuotaManager.instance().checkQuota(QuotaType.ADD_TAGS, true)){
            //Toast.makeText(context, R.string.create_second_place_delay, Toast.LENGTH_LONG).builder();
            return;
        }

        Intent intent = buildIntentAddTags(activity, event);
        activity.startActivityForResult(intent, REQUEST_TAGS);
    }

    public static Intent buildIntentAddTags(Context context, Event event){
        Intent intent = new Intent(context, AddTagActivity.class);
        Bundle extras = new Bundle();
        extras.putLong(IntentsUtils.KEY_EVENT, event.getId());
        intent.putExtras(extras);
        return intent;
    }

    public static void inviteFriendToEvent(Activity activity, Event event) {
        if (!requireLogin(activity, false))
            return;
        //if (!QuotaManager.instance().checkQuota(QuotaType.INVITE_FRIEND, true)){
            //Toast.makeText(context, R.string.create_second_place_delay, Toast.LENGTH_LONG).builder();
        //    return;
        //}
        Intent intent = new Intent(activity, InviteFriendsActivity.class);
        Bundle extras = new Bundle();
        extras.putLong(IntentsUtils.KEY_EVENT, event.getId());
        intent.putExtras(extras);
        activity.startActivityForResult(intent, REQUEST_INVITE_FRIENDS);
    }

    public static void viewEventFromId(Activity activity, long id) {
        Intent intent = buildIntentViewPlace(activity, id);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void viewSpecifiedEvent(Context context, Event event) {
        context.startActivity(buildIntentViewPlace(context, event));
    }


    public static void postEvent(Context context, Event event, int action) {
        if(!requireLogin(context,false)) {
            return;
        }
        if(!event.isUserAround() && ( action == ACTION_TAGS || action == ACTION_CAMERA ) ) {
            Toast.makeText(context, R.string.user_message_should_be_around_event_to_post, Toast.LENGTH_LONG).show();
            return;
        }
        if (context instanceof EventActivity){
            ((EventActivity)context).parseActionParameter(action);
            return;
        }
        Intent intent = buildIntentViewPlace(context, event);
        intent.putExtra(KEY_ACTION, action);
        context.startActivity(intent);
    }

    public static Intent buildIntentViewPlace(Context context, long eventId) {
        Intent intent = new Intent(context, EventActivity.class);
        intent.putExtra(KEY_EVENT_ID, eventId); // TODO cst
        return  intent;
    }
    public static Intent buildIntentViewPlace(Context context, Event event) {
        Intent intent = new Intent(context, EventActivity.class);
        Bundle extras = new Bundle();      // TODO use constant
        try {
            event = (Event) event.requireLocalId();
            extras.putLong(IntentsUtils.KEY_EVENT, event.getId());
        } catch (CannotSaveModelException e) {
            Log.e(TAG, e.getMessage());
        }
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
        if (!QuotaManager.instance().checkQuota(QuotaType.ADD_EVENT, true)){
            //Toast.makeText(context, R.string.create_second_place_delay, Toast.LENGTH_LONG).builder();
            return;
        }
        Intent intent = new Intent(activity, AddEventActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void pinSpot(Activity activity) {
        IntentsUtils.pinSpot(activity, null);
    }

    public static void pinSpot(Activity activity, Spot spot) {
        Log.d(TAG, "Trying to pin spot with location: " + LocationManager.getLastLocation());
        if (!LocationManager.hasFineLocation() || !LocationManager.hasUpToDateLastLocation()){
            Toast.makeText(MyApplication.getApplicationBaseContext(),
                    R.string.waiting_for_location, Toast.LENGTH_LONG).show();
            return;
        }
        if (!requireLogin(activity, false))
            return;
        if (!QuotaManager.instance().checkQuota(QuotaType.ADD_EVENT, true)){
            //Toast.makeText(context, R.string.create_second_place_delay, Toast.LENGTH_LONG).builder();
            return;
        }
        Intent intent = new Intent(activity, AddSpotActivity.class);
        if (spot != null && !spot.hasRemoteId()){
            intent.putExtra(IntentsUtils.KEY_SPOT, SerializeHelper.packModel(spot, Spot.class));
        }
        activity.startActivityForResult(intent, REQUEST_PICK_SPOT);
    }

    /**
     * Redirect to the last activity we attempt to go
     * before being redirected to the localLogin activity
     * TODO implement
     */
    public static void redirectToLastActivity(Activity activity) {
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
            Log.e(TAG, "Trying to extract a null event");
            return null;
        }
        return Event.load(Event.class, extras.getLong(IntentsUtils.KEY_EVENT));
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


    public static long extractPlaceId(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null){
            return -1;
        }
        return extras.getLong(KEY_EVENT_ID, Long.valueOf(extras.getString(KEY_EVENT_ID, "-1"))); // usefull for notifications
    }

    public static Spot extractSpot(Intent intent) {
        Bundle extras = intent.getExtras();
        try{
            return extras != null ? SerializeHelper.unpackModel(extras.getString(IntentsUtils.KEY_SPOT), Spot.class) : null;
        }
        catch (Exception ex){
            Log.e(TAG, "Error extracting spot: " + ex.getMessage());
            return null;
        }
    }

    public static void presentApp(Context context) {
        Intent intent = new Intent(context, PresentationActivity.class);
        context.startActivity(intent);
    }

    public static void actionShareApp(Context context) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, context.getString(R.string.share_message_text));
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(R.string.share_message_subject));
        context.startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }
}