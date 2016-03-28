package com.timappweb.timapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.ImageView;

import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.LocalPersistenceManager;
import com.timappweb.timapp.config.ServerConfiguration;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.SearchFilter;
import com.timappweb.timapp.entities.SocialProvider;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
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
    public static SearchFilter searchFilter = new SearchFilter();


    /**
     * @return true if user is logged in
     */
    public static boolean isLoggedIn(){
        if (currentUser != null){
            return true;
        }
        currentUser = getCurrentUser();
        return currentUser != null;
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
            int userId = LocalPersistenceManager.out().getInt(User.KEY_ID, -1);
            if (userId == -1){
                return null;
            }
            currentUser = new User();
            currentUser.id = userId;
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

    public static void login(User user, String token, String accessToken){
        setCurrentUser(user);
        LocalPersistenceManager.in().putBoolean(KEY_IS_LOGIN, true);
        LocalPersistenceManager.in().putInt(KEY_LOGIN_TIME, Util.getCurrentTimeSec());
        RestClient.instance().login(token);
        RestClient.instance().setSocialProvider(SocialProvider.FACEBOOK, accessToken);

        LocalPersistenceManager.in().commit();
    }

    private static Location lastLocation = null;
    public static ConfigurationProvider config;

    public static ServerConfiguration getServerConfig() {
        return config.getServerConfiguration();
    }

    public static ServerConfiguration.Rules getApplicationRules() {
        return config.getServerConfiguration().rules;
    }

    public static List<Category> getCategories() {
        return config.getServerConfiguration().categories;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        LocalPersistenceManager.init(this);
        RestClient.init(this, getResources().getString(R.string.ws_endpoint));

        //*******FACEBOOK******
        initFacebookPermissions();

        // Loading cache in memory
        com.timappweb.timapp.cache.CacheData.load();

        // Load configuration
        config = new ConfigurationProvider(getApplicationContext(), "configuration.properties", new ConfigurationProvider.Listener(){
            @Override
            public void onLoaded() {
                Log.d(TAG, "Server configuration has been loaded!");
                config.getServerConfiguration().initCategories(getApplicationContext());
            }

            @Override
            public void onFail() {
                Log.e(TAG, "Error cannot load server configuration");
            }
        });
        config.load();

    }

    private void initFacebookPermissions() {
        Permission[] permissions = new Permission[] {
                Permission.USER_PHOTOS,
                Permission.EMAIL,
                Permission.USER_FRIENDS,
        };
        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId(getResources().getString(R.string.app_id))
                .setNamespace(getResources().getString(R.string.namespace))
                .setPermissions(permissions)
                .build();
        SimpleFacebook.setConfiguration(configuration);
    }


    public static ImageView setCategoryBackground(ImageView i, int level) {
        switch (level) {
            case 0:
                i.setBackgroundResource(R.drawable.b1);
                return i;
            case 1:
                i.setBackgroundResource(R.drawable.b2);
                return i;
            case 2:
                i.setBackgroundResource(R.drawable.b3);
                return i;
            case 3:
                i.setBackgroundResource(R.drawable.b4);
                return i;
            case 4:
                i.setBackgroundResource(R.drawable.b5);
                return i;
            default:
                i.setBackgroundColor(0);
                return i;
        }
    }

    public static Category getCategoryById(int id) throws UnknownCategoryException {
        for (Category c: config.getServerConfiguration().categories){
            if (c.id == id){
                return c;
            }
        }
        throw new UnknownCategoryException(id);
    }

    public static Category getCategoryByIndex(int position) {
        return getCategories().get(position);
    }

    public static void redirectLogin(Context currentContext){
        Intent intent = new Intent(currentContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        currentContext.startActivity(intent);
    }

    public static void logout() {
        if (isLoggedIn()){
            LocalPersistenceManager.in().putBoolean(KEY_IS_LOGIN, false);
            LocalPersistenceManager.in().putInt(User.KEY_ID, -1);
            RestClient.instance().logoutUser();
            MyApplication.currentUser = null;
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
                (lastLocation.getTime() - System.currentTimeMillis()) < getApplicationRules().gps_min_time_delay;
    }

    /**
     * Check if there is a last location with a fine location
     * @return
     */
    public static boolean hasFineLocation() {
        return hasFineLocation(getApplicationRules().gps_min_accuracy);
    }

    public static boolean hasFineLocation(int minAccuracy) {
        return hasLastLocation() &&
                lastLocation.getAccuracy() <= minAccuracy;
    }

    public static Location getLastLocation() {
        return lastLocation;
    }

}
