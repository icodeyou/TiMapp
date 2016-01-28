package com.timappweb.timapp.config;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by stephane on 1/24/2016.
 */
public class Configuration {

    private static final String TAG = "AssetsPropertyReader";

    public static final String TAG_MIN_LENGTH = "tag.minlength";
    public static final String TAG_MAX_LENGTH = "tag.maxlength";
    public static final String TAG_MIN_SEARCH_LENGTH = "tag.minsearchlength";
    public static final String TAG_MAX_RESULT_SIZE = "tag.maxresults";
    public static final String PLACE_REACHABLE_DISTANCE = "place.reachabledistance";
    public static final java.lang.String GPS_MIN_ACCURACY = "gps.accuracy";
    public static final java.lang.String GPS_MIN_TIME_DELAY = "gps.time";
    public static final java.lang.String PLACE_MIN_LENGTH = "place.minlength";

    private Context context;
    private static Properties properties;

    public Configuration(Context context, String filename) {
        this.context = context;
        properties = new Properties();
        this.loadProperties(filename);
    }

    public void loadProperties(String FileName){
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(FileName);
            properties.load(inputStream);

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public int getInt(String name){
        return this.getInt(name, 0);
    }
    public int getInt(String name, int defaultValue){
        try{
            return Integer.parseInt(properties.getProperty(name));
        }
        catch (NumberFormatException ex){
            return defaultValue;
        }
    }

    public double getDouble(String name){
        return this.getDouble(name, 0.0);
    }
    public double getDouble(String name, double defaultValue){
        try{
            return Double.parseDouble(properties.getProperty(name));
        }
        catch (NumberFormatException ex){
            return defaultValue;
        }
    }
}
