package com.timappweb.timapp.utils.AreaDataCaching;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.utils.IntLatLng;
import com.timappweb.timapp.utils.IntLatLngBounds;
import com.timappweb.timapp.utils.IntPoint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by stephane on 9/12/2015.
 *
 * Discretize an area into a grid with int coordinates eg:
 *
 * -----------------------
 * (-1,1) | (0,1) | (1,1)
 * -----------------------
 * (-1,0) | (0,0) | (1,0)
 * -----------------------
 * (-1,-1)| (0,-1)| (1,-1)
 * -----------------------
 *
 * Each grid square has a @AreaRequestItem
 * The grid height and width are determined by the constructor inputs
 *
 * Optimisations available
 *      - Minimum area item size to prevent useless loading (MINIMUM_AREA_WIDTH, MINIMUM_AREA_HEIGHT)
 *      - Removing area items too far from area to load (MAXIMUM_ORIGIN_DISTANCE)
 *      - TODO: Prevent too much data loaded by dynamically reevaluating the maximum number of area loaded ?
 *      - Lazy updating of area item by only requesting news values (DELAY_BEFORE_UPDATE_REQUEST)
 *
 */
public class AreaRequestHistory{


    private static final String TAG                     = "AreaRequestHistory";
    public static int MAXIMUM_ORIGIN_DISTANCE           = 10;       // Means a 10*10 square from the origin point
    public static final int MAXIMUM_GRID_SIZE_ON_VIEW   = 2;        // Reset request history if we can put more than X area square on one screen
    public static int DELAY_BEFORE_UPDATE_REQUEST       = 60;       // seconds before checking for new tag again (0 = INFINITE)
    public static final int AREA_FACTOR_WIDTH           = 150;      // In percents
    public static final int AREA_FACTOR_HEIGHT          = 150;      // In percents
    public static final double MINIMUM_AREA_WIDTH       = 0.001;    // minimum area width in degrees
    public static final double MINIMUM_AREA_HEIGHT      = 0.001;    // minimum area width in degrees

    // ---------------------------------------------------------------------------------------------

    public int areaWidth;                                           // En degré (1 degré ~= 100 km)
    public int areaHeight;                                          // En degré
    public HashMap<IntPoint, AreaRequestItem> areas;
    private IntLatLng center;
    private AreaDataLoaderInterface dataLoader = null;

    // ---------------------------------------------------------------------------------------------

    public AreaRequestHistory(LatLngBounds bounds, AreaDataLoaderInterface dataLoader) {
        Log.d(TAG, "Building a new AreaRequestHistory with bounds: " + bounds);
        this.setAreaSize(bounds);
        this.areas = new HashMap<>();
        this.dataLoader = dataLoader;
    }

    private void resizeArea(LatLngBounds bounds) {
        Log.d(TAG, "Resizing area request history base area: " + bounds);
        this.dataLoader.clearAll();
        this.areas.clear();
        this.setAreaSize(bounds);
    }

    private void setAreaSize(LatLngBounds bounds){
        IntLatLngBounds intBounds = new IntLatLngBounds(bounds);
        int areaWidth = intBounds.getWidth();
        int areaHeight = intBounds.getHeight();
        areaWidth = Math.max(CoordinateConverter.convert(MINIMUM_AREA_WIDTH), areaWidth);
        areaHeight = Math.max(CoordinateConverter.convert(MINIMUM_AREA_HEIGHT), areaHeight);

        this.areaWidth = (areaWidth * AREA_FACTOR_WIDTH) / 100;
        this.areaHeight = (areaHeight *  AREA_FACTOR_HEIGHT) / 100;

        int paddingWidth = (this.areaWidth - areaWidth) / 2;
        int paddingHeight = (this.areaHeight - areaHeight) / 2;

        this.center = new IntLatLng(bounds.southwest);
        this.center.latitude -= paddingHeight;
        this.center.longitude -= paddingWidth;

        Log.i(TAG, "New area size: " + intBounds.getMeterWidth() + "m x " + intBounds.getMeterHeight() + "m");
        Log.d(TAG, "Height:" + areaHeight + " + " + 2*paddingHeight + " = " + this.areaHeight);
        Log.d(TAG, "Width:" + areaWidth + " + " + 2 * paddingWidth + " = " + this.areaWidth);
    }

    // ---------------------------------------------------------------------------------------------

    public IntPoint getIntPoint(LatLng location){
        return this.getIntPoint(new IntLatLng(location));
    }

    /**
     * Get the area coordinate that contains the name
     *
     * @param location
     * @return
     */
    public IntPoint getIntPoint(IntLatLng location){
        int x = (int)((location.longitude - center.longitude) / areaWidth);
        int y = (int)((location.latitude - center.latitude) / areaHeight);
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

    /**
     * Update or create an history for one area
     * @param p: the south west point for the area
     * @param item: the area request item
     */
    private void update(IntPoint p, AreaRequestItem item){
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

    /**
     * Update all areas inside the bound
     * @param bounds
     */
    public void update(LatLngBounds bounds) {
        IntPoint northeastPoint = getIntPoint(bounds.northeast);
        IntPoint southeastPoint = getIntPoint(bounds.southwest);

        if (southeastPoint.distance(northeastPoint) > MAXIMUM_GRID_SIZE_ON_VIEW) {
            this.resizeArea(bounds);
        }
        else {
            clearTooFarAreas(bounds);
        }

        AreaIterator areaIterator = this.getAreaIterator(bounds);
        IntPoint p;
        while ((p = areaIterator.next()) != null){
            IntPoint pCpy = new IntPoint(p);
            AreaRequestItem request = this.areas.get(p);
            if (request != null){
                if (this.DELAY_BEFORE_UPDATE_REQUEST > 0 && request.getLastUpdateDelay() >= this.DELAY_BEFORE_UPDATE_REQUEST){
                    QueryCondition conditions = new QueryCondition();
                    Log.i(TAG, "-> " + p + " Data in cache are too old; updating with new data from dataTimestamp: " + request.dataTimestamp);
                    conditions.setBounds(this.getBoundFromPoint(p).toDouble());
                    conditions.setTimestampMin(request.dataTimestamp);
                    request.updateLocalTimestamp();
                    dataLoader.load(pCpy, request, conditions);
                }
                else{
                    Log.i(TAG, "-> " + p + " Data in cache are up to date (last update: " +request.getLastUpdateDelay()+ " seconds)");
                }
            }
            else{
                Log.i(TAG, "-> " + p + "  No data in cache; we need a server request");
                request = new AreaRequestItem();
                this.update(pCpy, request);
                // We need to build a new condition object because multi threading
                QueryCondition conditions = new QueryCondition();
                conditions.setBounds(this.getBoundFromPoint(p).toDouble());
                request.updateLocalTimestamp();
                dataLoader.load(pCpy, request, conditions);
            }
        }
    }

    public List<Post> clearTooFarAreas(LatLngBounds bounds){
        IntPoint southeastPoint = getIntPoint(bounds.southwest);

        List<Post> dataToClear = new LinkedList<>();
        Iterator it = areas.entrySet().iterator();
        int distance;
        while (it.hasNext()) {
            Map.Entry<IntPoint, AreaRequestItem> entry = (Map.Entry) it.next();
            distance = entry.getKey().distance(southeastPoint);
            if (distance > MAXIMUM_ORIGIN_DISTANCE) {
                Log.i(TAG, "Post caching too far from origin. Clearing spot history and markers");
                dataToClear.addAll(entry.getValue().data);
                it.remove();
            }
        }
        if (dataToClear.size() > 0){
            this.dataLoader.clear(dataToClear);
        }

        return dataToClear;
    }


    public IntLatLng getCenter() {
        return center;
    }

    public void clearAll() {
        for (AreaRequestItem area: this.areas.values()) {
            area.cancel();
        }
    }
}
