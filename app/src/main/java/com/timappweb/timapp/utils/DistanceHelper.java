package com.timappweb.timapp.utils;

import android.location.Location;

/**
 * Created by stephane on 1/26/2016.
 */
public class DistanceHelper {

    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = (float) (earthRadius * c);

        return dist;
    }

    public static double distFrom(Location newLocation, Location lastLocation) {
        return distFrom(newLocation.getLatitude(), newLocation.getLongitude(), lastLocation.getLatitude(), lastLocation.getLongitude());
    }
}
