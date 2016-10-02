package com.timappweb.timapp.utils;

import android.content.res.Resources;
import android.location.Location;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by stephane on 1/26/2016.
 */
public class DistanceHelper {

    public static final double EARTH_RADIUS = 6371000; // Meters
    public static final double METER_PER_LATITUDE = 110574;  // Meters

    public static final double getMeterPerLongitude(double latitude){
        return 111.320 * Math.cos(Math.toRadians(latitude));
    }
    public static double metersToLongitude(double meters, double latitude) {
        return meters/getMeterPerLongitude(latitude);
    }
    public static double metersToLatitude(double meters) {
        return meters/METER_PER_LATITUDE;
    }

    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = (float) (EARTH_RADIUS * c);

        return dist;
    }

    public static double distFrom(Location newLocation, Location lastLocation) {
        return distFrom(newLocation.getLatitude(), newLocation.getLongitude(), lastLocation.getLatitude(), lastLocation.getLongitude());
    }

    public static String prettyPrint(double dist) {
        String distString = String.valueOf(dist);
        if (dist < 30){
            return MyApplication.getApplicationBaseContext().getResources().getString(R.string.next_to_you);
        }
        else if (dist<1000) {
            return distString + " m";
        }
        else {
            double distKm = dist/1000;
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.HALF_DOWN);
            String roundedDistKm = df.format(distKm);
            return roundedDistKm + " km";
        }
    }
}
