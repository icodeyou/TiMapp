package com.timappweb.timapp.utils.AreaDataCaching;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.utils.IntLatLng;
import com.timappweb.timapp.utils.IntLatLngBounds;
import com.timappweb.timapp.utils.IntPoint;

import java.util.HashMap;

/**
 * Created by stephane on 9/12/2015.
 */
public class AreaRequestHistory{

    private static final String TAG                     = "AreaRequestHistory";

    public static final int MAXIMUM_ORIGIN_DISTANCE     = 10;   // Means a 10*10 square from the origin point
    public static final int MAXIMUM_GRID_SIZE_ON_VIEW   = 2;
    public static final int DELAY_BEFORE_UPDATE_REQUEST = 60;   // seconds before checking for new tag again
    public static final int AREA_FACTOR_WIDTH           = 150;  // In percents
    public static final int AREA_FACTOR_HEIGHT          = 150;  // In percents

    public int areaWidth                                = 1000; // En degré / 100000 (1 degré ~= 100 km)
    public int areaHeight                               = 1000; // En degré / 100000



    public HashMap<IntPoint, AreaRequestItem> areas;
    private IntLatLng center;

    private AreaDataLoaderInterface dataLoader = null;

    public AreaRequestHistory(int areaWidth, int areaHeight, IntLatLng center, AreaDataLoaderInterface dataLoader) {
        this.areaWidth = (areaWidth * AREA_FACTOR_WIDTH) / 100;
        this.areaHeight = (areaHeight *  AREA_FACTOR_WIDTH) / 100;

        int paddingWidth = (this.areaWidth - areaWidth) / 2;
        int paddingHeight = (this.areaHeight - areaHeight) / 2;
        this.center = center;
        this.center.latitude -= paddingHeight;
        this.center.longitude -= paddingWidth;


        Log.d(TAG, "Height:" + areaHeight + " + " + 2*paddingHeight + " = " + this.areaHeight);
        Log.d(TAG, "Width:" + areaWidth + " + " + 2*paddingWidth + " = " + this.areaWidth);
        this.areas = new HashMap<>();
        this.dataLoader = dataLoader;
    }

    public IntPoint getIntPoint(LatLng location){
        return this.getIntPoint(new IntLatLng(location));
    }

    public IntPoint getIntPoint(IntLatLng location){
        int x = (int)((location.longitude - center.longitude) / areaWidth);
        int y = (int)((location.latitude - center.latitude) / areaHeight);
        // Check if we are too far from the last point loaded
        return new IntPoint(y, x);
    }

    /**
     * From the id point in cache, convert it as a bounds
     * @param p
     * @return
     */
    public IntLatLngBounds getBoundFromPoint(IntPoint p){
        int latitude = this.center.latitude + (p.y * areaHeight);
        int longitude = this.center.longitude + (p.x * areaWidth);
        return new IntLatLngBounds(new IntLatLng(latitude, longitude), new IntLatLng(latitude+ areaHeight, longitude+ areaWidth));
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

    /**
     * Return iterator for different area
     * @param bounds
     * @return
     */
    public AreaIterator getAreaIterator(LatLngBounds bounds) {
        IntPoint northeast = getIntPoint(bounds.northeast);
        IntPoint southwest = getIntPoint(bounds.southwest);
        Log.i(TAG, "Southwest is " + southwest.toString() + "Northeast point is " + northeast.toString() + " (in cache: " + areas.size() + ")");
        return new AreaIterator(southwest, northeast);
    }

    public void updateData(LatLngBounds bounds) {
        AreaIterator areaIterator = this.getAreaIterator(bounds);
        IntPoint p;
        while ((p = areaIterator.next()) != null){
            IntPoint pCpy = new IntPoint(p);
            AreaRequestItem request = this.areas.get(p);
            if (request != null){
                if (request.getLastUpdateDelay() > this.DELAY_BEFORE_UPDATE_REQUEST){
                    QueryCondition conditions = new QueryCondition();
                    Log.i(TAG, "-> " + p + " Data in cache are too old; updating with new data from timestamp: " + request.timestamp);
                    conditions.setBounds(this.getBoundFromPoint(p).toDouble());
                    conditions.setTimestampMin(request.timestamp);
                    dataLoader.load(pCpy, request, conditions);
                }
                else{
                    Log.i(TAG, "-> " + p + " Data in cache are up to date (last update: " +request.getLastUpdateDelay()+ ")");
                }
            }
            else{
                Log.i(TAG, "-> " + p + "  No data in cache; we need a server request");
                request = new AreaRequestItem();
                this.update(pCpy, request);
                // We need to build a new condition object because multi threading
                QueryCondition conditions = new QueryCondition();
                conditions.setBounds(this.getBoundFromPoint(p).toDouble());
                dataLoader.load(pCpy, request, conditions);
            }
        }
    }


}
