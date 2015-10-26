package com.timappweb.timapp;

import android.app.Application;

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

    public static boolean isLoggedIn(){
        return LocalPersistenceManager.instance.pref.getBoolean(RestClient.IS_LOGIN, false);
    }


}
