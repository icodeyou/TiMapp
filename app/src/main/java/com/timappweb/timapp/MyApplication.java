package com.timappweb.timapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.cache.CacheData;
import com.timappweb.timapp.config.Configuration;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.LocalPersistenceManager;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.utils.Util;

import java.util.LinkedList;
import java.util.List;

public class MyApplication extends Application{

    private static final String TAG = "MyApplication";
    public static final String KEY_IS_LOGIN = "IsLoggedIn";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final String KEY_LOGIN_TIME = "LoginTime";

    private static User currentUser = null;


    /**
     * @return true if user is logged in
     */
    public static boolean isLoggedIn(){
        if (currentUser != null){
            return true;
        }
        return LocalPersistenceManager.out().getBoolean(MyApplication.KEY_IS_LOGIN, false);
    }

    public static void checkToken(Context context){
        int loginTime = LocalPersistenceManager.out().getInt(MyApplication.KEY_LOGIN_TIME, 0);
        if (Util.isOlderThan(loginTime, 3600)){
            RestClient.instance().checkToken(new RestFeedbackCallback(context) {
                @Override
                public void onActionSuccess(RestFeedback feedback) {
                    Log.i(TAG, "Token is still valid.");
                    LocalPersistenceManager.in().putInt(KEY_LOGIN_TIME, Util.getCurrentTimeSec());
                }

                @Override
                public void onActionFail(RestFeedback feedback) {
                    Log.i(TAG, "Token is not valid anymore. Login out");
                    MyApplication.logout();
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.i(TAG, "Server is not available. Login out");
                    MyApplication.logout();
                }
            });
        }
    }

    public static User getCurrentUser(){
        if (currentUser != null){
            return currentUser;
        }
        else {
            currentUser = new User();
            currentUser.id = LocalPersistenceManager.out().getInt(User.KEY_ID, -1);
            currentUser.email = LocalPersistenceManager.out().getString(User.KEY_EMAIL, null);
            currentUser.username = LocalPersistenceManager.out().getString(User.KEY_NAME, null);
            Log.d(TAG, "Loading user form pref: " + currentUser);
            return currentUser;
        }
    }

    public static void setCurrentUser(User user){
        Log.d(TAG, "Writing user form pref: " + user);
        LocalPersistenceManager.in().putInt(User.KEY_ID, user.id);
        LocalPersistenceManager.in().putString(User.KEY_NAME, user.username);
        LocalPersistenceManager.in().putString(User.KEY_EMAIL, user.email);
        currentUser = user;
    }

    public static void login(User user, String token){
        setCurrentUser(user);
        LocalPersistenceManager.in().putBoolean(KEY_IS_LOGIN, true);
        LocalPersistenceManager.in().putInt(KEY_LOGIN_TIME, Util.getCurrentTimeSec());
        RestClient.instance().login(token);

        LocalPersistenceManager.in().commit();
    }

    public static List<Category> categories = new LinkedList<>();
    private static Location lastLocation = null;
    public static Configuration config;

    @Override
    public void onCreate(){
        super.onCreate();
        LocalPersistenceManager.init(this);

        String endpoint = getResources().getString(R.string.ws_endpoint);
        RestClient.init(this, endpoint);

        initCategories();

        // Load configuration
        config = new Configuration(getApplicationContext(), "configuration.properties"); // TODO use ressource

        //*******FACEBOOK******
        initFacebookPermissions();

        // Loading cache in memory
        CacheData.load();
    }

    private void initFacebookPermissions() {
        Permission[] permissions = new Permission[] {
                Permission.USER_PHOTOS,
                Permission.EMAIL,
        };
        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId(getResources().getString(R.string.app_id))
                .setNamespace(getResources().getString(R.string.namespace))
                .setPermissions(permissions)
                .build();
        SimpleFacebook.setConfiguration(configuration);
    }

    public void initCategories(){
        categories.add(new Category(1, "music", R.drawable.ic_category_music));
        categories.add(new Category(2, "strike", R.drawable.ic_category_strike));
        categories.add(new Category(3, "streetshow", R.drawable.ic_category_streetshow));
        categories.add(new Category(4, "sport", R.drawable.ic_category_sport));
        categories.add(new Category(5, "bar", R.drawable.ic_category_bar));
        categories.add(new Category(6, "show", R.drawable.ic_category_show));
        categories.add(new Category(7, "party", R.drawable.ic_category_party));
        categories.add(new Category(8, "unknown", R.drawable.ic_category_unknown));
    }

    public static Category getCategory(int id){
        for (Category c: categories){
            if (c.id == id){
                return c;
            }
        }
        return  null;
    }


    public static void redirectLogin(Context currentContext){
        Intent intent = new Intent(currentContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        currentContext.startActivity(intent);
    }

    public static void logout() {
        if (isLoggedIn()){
            LocalPersistenceManager.in().putBoolean(KEY_IS_LOGIN, false);
            RestClient.instance().logoutUser();
        }
    }
    public static void setLastLocation(Location l) {
        Log.i(TAG, "Location has changed: " + Util.print(l));
        Log.d(TAG, "Last location accuracy: " + l.getAccuracy());
        lastLocation = l;
    }

    /**
     * Check if there is a last location that is not outdated
     * @return
     */
    public static boolean hasLastLocation() {
        return lastLocation != null &&
                (lastLocation.getTime() - System.currentTimeMillis()) < MyApplication.config.getInt(Configuration.GPS_MIN_TIME_DELAY, 10000);
    }

    /**
     * Check if there is a last location with a fine location
     * @return
     */
    public static boolean hasFineLocation() {
        return hasLastLocation() &&
                lastLocation.getAccuracy() <= MyApplication.config.getInt(Configuration.GPS_MIN_ACCURACY);
    }

    public static Location getLastLocation() {
        return lastLocation;
    }




    /*
    private static AlertDialog alertDialog = null;

    static AlertDialog dialog = null;
    public static void showAlert(Context context, String message) {
        if (dialog == null){
            Log.d(TAG, "Creating new dialog");
            dialog = (new AlertDialog.Builder(context)).create();
        }
        if (!dialog.isShowing()){
            dialog.setMessage(message);
            dialog.show();
        }
    }

    public static void showAlert(Context context, int id){
        showAlert(context, context.getResources().getString(id));
    }
    */
}
