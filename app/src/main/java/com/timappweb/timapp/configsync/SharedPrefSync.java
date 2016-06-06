package com.timappweb.timapp.configsync;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.timappweb.timapp.rest.RestClient;

/**
 * Created by stephane on 4/21/2016.
 */
public class SharedPrefSync implements LocalPersistenceManager {

    private final Gson gson;
    public SharedPreferences pref;
    private String key;

    public <T> SharedPrefSync(String key, SharedPreferences pref) {
        this.pref = pref;
        this.key = key;
        // TODO move as a parameter
        this.gson = RestClient.instance().getGson();
    }

    @Override
    public void write(SyncConfig data) {
        String json = gson.toJson(data); // myObject - instance of MyObject
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, json);
        editor.commit();
    }

    @Override
    public SyncConfig load() {
        Gson gson = new Gson();
        String json = this.pref.getString(key, null);
        if (json == null) return null;
        return gson.fromJson(json, SyncConfig.class);
    }

    @Override
    public void clear() {
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key);
        editor.commit();
    }
}
