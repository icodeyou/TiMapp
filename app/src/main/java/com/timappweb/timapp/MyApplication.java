package com.timappweb.timapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.config.Configuration;
import com.timappweb.timapp.data.LocalPersistenceManager;
import com.timappweb.timapp.entities.Category;
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.Util;

import java.util.LinkedList;
import java.util.List;

public class MyApplication extends Application{

    private static final String TAG = "MyApplication";
    public static User getCurrentUser(){
        return RestClient.instance().getCurrentUser();
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
        categories.add(new Category(2, "bar", R.drawable.ic_category_bar));
        categories.add(new Category(3, "streetshow", R.drawable.ic_category_streetshow));
        categories.add(new Category(4, "sport", R.drawable.ic_category_sport));
        categories.add(new Category(5, "strike", R.drawable.ic_category_strike));
        categories.add(new Category(6, "show", R.drawable.ic_category_show));
        categories.add(new Category(7, "house", R.drawable.ic_category_house));
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

    /**
     * @return true if user is logged in
     */
    public static boolean isLoggedIn(){
        boolean isLoggedIn = LocalPersistenceManager.instance.pref.getBoolean(RestClient.IS_LOGIN, false);
        return isLoggedIn;
    }


    /**
     * If user is logged in do nothing
     * If not redirect to login page
     * @param currentContext
     */
    public static boolean requireLoggedIn(Context currentContext) {
        if (!isLoggedIn()){
            redirectLogin(currentContext);
            return false;
        }
        return true;
    }

    public static void redirectLogin(Context currentContext){
        Intent intent = new Intent(currentContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        currentContext.startActivity(intent);
    }

    public static void logout() {
        if (isLoggedIn()){
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
