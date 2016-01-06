package com.timappweb.timapp.data;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by stephane on 8/29/2015.
 */
public class LocalPersistenceManager {
    private static final String TAG = "LocalPersistenceManager";

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared pref file name
    private static final String PREF_NAME = "com.timapp.pref";

    // Editor for Shared preferences
    public SharedPreferences.Editor editor;
    public SharedPreferences pref;

    private LocalPersistenceManager(Application app) {
        pref = app.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();

        Log.i(TAG, "Share preference done!");
    }

    public static LocalPersistenceManager instance;

    public static void init(Application app){
        instance = new LocalPersistenceManager(app);
    }
}
