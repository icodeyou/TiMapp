package com.timappweb.timapp.config;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by stephane on 8/29/2015.
 */
public class LocalPersistenceManager {
    private static final String TAG = "LocalPersistenceManager";
    private static final String PREF_NAME = "com.timapp.pref";

    // Shared pref mode
    int PRIVATE_MODE = 0;

    public SharedPreferences.Editor editor;
    public SharedPreferences pref;

    private LocalPersistenceManager(Application app) {
        pref = app.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        Log.i(TAG, "Loading share preference done!");
    }

    public static LocalPersistenceManager instance;

    public static void init(Application app){
        instance = new LocalPersistenceManager(app);
    }

    public static SharedPreferences.Editor in() {
        return instance.editor;
    }

    public static SharedPreferences out() {
        return instance.pref;
    }

}
