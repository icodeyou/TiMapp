package com.timappweb.timapp.utils;

import android.location.Location;
import android.location.LocationListener;

/**
 * Created by stephane on 9/10/2015.
 */
public class Util {

    public static String print(Location location) {
        return location.getLongitude()+"-"+location.getLatitude()+ " (Accuracy"+location.getAccuracy()+")";
    }

    public static int getCurrentTimeSec() {
        return (int)(System.currentTimeMillis() / 1000);
    }
}
