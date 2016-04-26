package com.timappweb.timapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.timappweb.timapp.rest.RestClient;

/**
 * Created by stephane on 4/21/2016.
 */
public class KeyValueStorage {

    public static final int SHARED_PREF_PRIVATE_MODE = 0;

    public static KeyValueStorage instance = null;
    private static SharedPreferences.Editor editor;

    private Gson gson;
    private SharedPreferences pref;

    public static void init(Context context, Gson gson){
        instance = new KeyValueStorage(context.getSharedPreferences(context.getPackageName() + "_shared_pref", SHARED_PREF_PRIVATE_MODE), gson);
    }

    private KeyValueStorage(SharedPreferences pref, Gson gson) {
        this.pref = pref;
        this.gson = gson;
    }

    public <T> void set(String key, T data) {
        String json = gson.toJson(data); // myObject - instance of MyObject
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, json);
        editor.commit();
    }

    public <T> T get(String key, Class<T> type) {
        Gson gson = new Gson();
        String json = this.pref.getString(key, null);
        if (json == null) return null;
        return (T) gson.fromJson(json, type);
    }

    public void clear(String key) {
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key);
        editor.commit();
    }

    public boolean exists(String key) {
        return  pref.contains(key);
    }

    public static SharedPreferences.Editor in() {
        if (editor == null){
            editor = instance.pref.edit();
        }
        return editor;
    }

    public static SharedPreferences out() {
        return instance.pref;
    }

    public static void clear(String... keys) {
        SharedPreferences.Editor edit = instance.pref.edit();
        for(String key: keys){
            edit.remove(key);
        }
        edit.commit();
    }

    public static void putString(String key, String value) {
        SharedPreferences.Editor edit = instance.pref.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public static long getSafeLong(String key, long defaultValue) {
        try {
            return instance.pref.getLong(key, defaultValue);
        }
        catch (Exception ex){
            instance.clear(key);
            return defaultValue;
        }

    }
}
