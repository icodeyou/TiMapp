package com.timappweb.timapp.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.location.MyLocationProvider;

/**
 * Created by stephane on 6/7/2016.
 */
public class MapFactory {

    public static void initMap(GoogleMap gMap, boolean gestureEnabled){
        if (MyLocationProvider.hasLocationPermission()){
            gMap.setMyLocationEnabled(true);
        }
        gMap.setIndoorEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(false);
        gMap.getUiSettings().setMapToolbarEnabled(false);
        gMap.getUiSettings().setCompassEnabled(false);
        gMap.getUiSettings().setScrollGesturesEnabled(gestureEnabled);
        gMap.getUiSettings().setRotateGesturesEnabled(gestureEnabled);
        gMap.getUiSettings().setTiltGesturesEnabled(gestureEnabled);
        gMap.getUiSettings().setZoomGesturesEnabled(gestureEnabled);
    }
}
