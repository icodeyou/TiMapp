package com.timappweb.timapp.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

/**
 * Created by stephane on 6/7/2016.
 */
public class MapFactory {

    public static void initMap(GoogleMap gMap){
        gMap.setMyLocationEnabled(true);
        gMap.setIndoorEnabled(true);
    }
}
