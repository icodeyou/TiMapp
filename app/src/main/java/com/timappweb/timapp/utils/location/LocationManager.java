package com.timappweb.timapp.utils.location;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.exceptions.NoLastLocationException;
import com.timappweb.timapp.fragments.ExploreMapFragment;
import com.timappweb.timapp.utils.DistanceHelper;
import com.timappweb.timapp.utils.Util;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 5/16/2016.
 */
public class LocationManager {

    private static final String TAG = "LocationManager";

    // =============================================================================================

    private static Location                 lastLocation        = null;
    private static com.google.android.gms.location.LocationListener     mLocationListener   = null;
    private static MyLocationProvider       locationProvider    = null;
    private static List<LocationListener>   listeners           = new LinkedList<>();

    // =============================================================================================

    public static void setLastLocation(Location l) {
        Log.v(TAG, "Location has changed: " + l.toString());
        lastLocation = l;
    }

    /**
     * Check if there is a last location that is not outdated
     * @return
     */
    public static boolean hasLastLocation() {
        return lastLocation != null;
    }

    public static boolean hasUpToDateLastLocation(){
        return lastLocation != null &&
            (System.currentTimeMillis() - lastLocation.getTime()) < ConfigurationProvider.rules().gps_min_time_delay;
    }

    public static MyLocationProvider getLocationProvider() {
        return locationProvider;
    }
    /**
     * Check if there is a last location with a fine location
     * @return
     */
    public static boolean hasFineLocation() {
        return hasFineLocation(ConfigurationProvider.rules().gps_min_accuracy);
    }

    public static boolean hasFineLocation(int minAccuracy) {
        return hasLastLocation() &&
                lastLocation.getAccuracy() <= minAccuracy;
    }

    public static Location getLastLocation() {
        return lastLocation;
    }


    private static void initLocationListener(){
        if (mLocationListener != null) return;
        mLocationListener = new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Location tmpLocation = lastLocation; // TODO BUG copy ?
                setLastLocation(location);
                for (LocationListener listener : listeners){
                    listener.onLocationChanged(location, tmpLocation);
                }
            }
        };
    }


    public static void initLocationProvider(Activity activity) {
        locationProvider = new MyLocationProvider(activity, mLocationListener);

        if (!locationProvider.isGPSEnabled()){
            locationProvider.askUserToEnableGPS();
        }

        locationProvider.requestMultipleUpdates();
    }

    public static void addOnLocationChangedListener(LocationListener locationListener) {
        if (!listeners.contains(locationListener)){
            listeners.add(locationListener);
        }
    }


    public static void start(Activity activity){
        Log.d(TAG, "Starting location manager");
        initLocationListener();
        initLocationProvider(activity);
        locationProvider.connect();
        try {
            lastLocation = locationProvider.getLastLocation();
        } catch (NoLastLocationException e) {

        }
    }

    public static void stop(){
        Log.d(TAG, "Stopping location manager");
        listeners.clear();
        if (locationProvider != null) {
            locationProvider.disconnect();
        }

    }

    public static LatLngBounds generateBoundsAroundLocation(Location location, int size) {
        return generateBoundsAroundLocation(location.getLatitude(), location.getLongitude(), size);
    }

    public static LatLngBounds generateBoundsAroundLocation(double latitude, double longitude, int size) {
        double offsetLatitude = DistanceHelper.metersToLatitude(size) / 2;
        double offsetLongitude = DistanceHelper.metersToLongitude(size, latitude) / 2;
        return new LatLngBounds(
                new LatLng(latitude - offsetLatitude, longitude - offsetLongitude),
                new LatLng(latitude + offsetLatitude, longitude + offsetLongitude));

    }
    public static LatLngBounds expand(LatLngBounds bounds, int size) {
        LatLng southwest =new LatLng(
                bounds.southwest.latitude - DistanceHelper.metersToLatitude(size),
                bounds.southwest.longitude - DistanceHelper.metersToLongitude(size, bounds.southwest.latitude)
        );
        LatLng northeast =new LatLng(
                bounds.northeast.latitude + DistanceHelper.metersToLatitude(size),
                bounds.northeast.longitude + DistanceHelper.metersToLongitude(size, bounds.northeast.latitude)
        );
        LatLngBounds newBounds = new LatLngBounds(southwest, northeast);
        return newBounds;
    }

    public static void removeLocationListener(Object object) {
        listeners.remove(object);
    }



    // =============================================================================================

    public interface LocationListener{
        void onLocationChanged(Location newLocation, Location lastLocation);
    }

}
