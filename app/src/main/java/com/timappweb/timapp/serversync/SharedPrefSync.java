package com.timappweb.timapp.serversync;

import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * Created by stephane on 4/21/2016.
 */
public class SharedPrefSync implements LocalPersistenceManager {

    private final Gson gson;
    public SharedPreferences pref;
    private String key;

    public SharedPrefSync(String key, SharedPreferences pref) {
        this.pref = pref;
        this.key = key;
        this.gson = new Gson();
    }

    @Override
    public void write(SyncConfigManager.SyncConfig data) {
        String json = gson.toJson(data); // myObject - instance of MyObject
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, json);
        editor.commit();
    }

    @Override
    public SyncConfigManager.SyncConfig load() {
        Gson gson = new Gson();
        String json = this.pref.getString(key, null);
        if (json == null) return null;
        return gson.fromJson(json, SyncConfigManager.SyncConfig.class);
    }
}
