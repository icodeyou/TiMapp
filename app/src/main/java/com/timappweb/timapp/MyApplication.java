package com.timappweb.timapp;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.data.LocalPersistenceManager;
import com.timappweb.timapp.rest.RestClient;

/**
 * Created by stephane on 8/21/2015.
 */
public class MyApplication extends Application{

    @Override
    public void onCreate(){
        super.onCreate();
        LocalPersistenceManager.init(this);

        String endpoint = getResources().getString(R.string.ws_endpoint);
        RestClient.init(this, endpoint);
    }

    /**
     * @return true if user is logged in
     */
    public static boolean isLoggedIn(){
        boolean isLoggedIn = LocalPersistenceManager.instance.pref.getBoolean(RestClient.IS_LOGIN, false);
        return isLoggedIn;
        // TODO
        //if (isLoggedIn){
        //    int lastLoggedIn = LocalPersistenceManager.instance.pref.getInt(RestClient.LAST_LOGGED_IN, 0);
        //    return lastLoggedIn < RestClient.LOGIN
        //}
        //return false;
    }


    /**
     * If user is logged in do nothing
     * If not redirect to login page
     * @param activity
     */
    public static boolean requireLoggedIn(Activity activity) {
        if (!isLoggedIn()){
            Intent intent = new Intent(activity, LoginActivity.class);
            activity.startActivity(intent);
            return false;
        }
        return true;
    }
}
