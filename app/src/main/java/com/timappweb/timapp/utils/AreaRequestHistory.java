package com.timappweb.timapp.utils;

import android.graphics.Point;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.activities.MapsActivity;
import com.timappweb.timapp.entities.Spot;
import com.timappweb.timapp.fragments.MapsFragment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Created by stephane on 9/12/2015.
 */
public class AreaRequestHistory{

    private static final String TAG = "AreaRequestHistory";
    public static final int MAXIMUM_ORIGIN_DISTANCE = 10; // Means a 10*10 square from the origin point
    public static final int MAXIMUM_GRID_SIZE_ON_VIEW = 2;
    public static final int DELAY_BEFORE_UPDATE_REQUEST = 60; // seconds before checking for new tag again
    public int AREA_WIDTH = 1000; // En degré (1 degré ~= 100 km)
    public int AREA_HEIGHT = 1000; // En degré
    public HashMap<IntPoint, AreaRequestItem> areas;
    private IntLatLng center;

    public AreaRequestHistory(int AREA_WIDTH, int AREA_HEIGHT, IntLatLng center) {
        this.AREA_WIDTH = AREA_WIDTH;
        this.AREA_HEIGHT = AREA_HEIGHT;
        this.center = center;
        this.areas = new HashMap<>();
    }

    public IntPoint getIntPoint(LatLng location){
        return this.getIntPoint(new IntLatLng(location));
    }

    public IntPoint getIntPoint(IntLatLng location){
        int x = (int)((location.longitude - center.longitude) / AREA_WIDTH);
        int y = (int)((location.latitude - center.latitude) / AREA_HEIGHT);
        // Check if we are too far from the last point loaded
        return new IntPoint(y, x);
    }

    /**
     * From the id point in cache, convert it as a bounds
     * @param p
     * @return
     */
    public IntLatLngBounds getBoundFromPoint(IntPoint p){
        int latitude = this.center.latitude + (p.y * AREA_HEIGHT);
        int longitude = this.center.longitude + (p.x * AREA_WIDTH);
        return new IntLatLngBounds(new IntLatLng(latitude, longitude), new IntLatLng(latitude+AREA_HEIGHT, longitude+AREA_WIDTH));
    }

    public void update(IntPoint p, AreaRequestItem item){
        if (areas.containsKey(p)){
            Log.i(TAG, "Updating request history: " + item);
            areas.get(p).update(item);
        }
        else{
            Log.i(TAG, "Creating request history: " + item);
            areas.put(p, item);
        }
    }



}
