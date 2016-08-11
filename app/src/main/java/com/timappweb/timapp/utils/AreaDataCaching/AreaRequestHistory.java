package com.timappweb.timapp.utils.AreaDataCaching;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.data.entities.MarkerValueInterface;
import com.timappweb.timapp.rest.model.QueryCondition;
import com.timappweb.timapp.utils.IntLatLng;
import com.timappweb.timapp.utils.IntLatLngBounds;
import com.timappweb.timapp.utils.IntPoint;
import com.timappweb.timapp.utils.Util;

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
 * Each grid square has a @RAMAreaRequestItem
 * The grid height and width are determined by the constructor inputs
 *
 * Optimisations available
 *      - Minimum area item size to prevent useless loading (MINIMUM_AREA_WIDTH, MINIMUM_AREA_HEIGHT)
 *      - Removing area items too far from area to load (MAXIMUM_ORIGIN_DISTANCE)
 *      - TODO: Prevent too much data loaded by dynamically reevaluating the maximum number of area loaded ?
 *      - Lazy updating of area item by only requesting news values (DELAY_BEFORE_UPDATE_REQUEST)
 *
 */
public class AreaRequestHistory<T extends MarkerValueInterface>{


    private static final String     TAG                         = "AreaRequestHistory";
    public static int               MAXIMUM_ORIGIN_DISTANCE     = 10;       // Means a 10*10 square from the origin point
    public static final int         MAXIMUM_GRID_SIZE_ON_VIEW   = 2;        // Reset request history if we can put more than X area square on one screen
    public static int               DELAY_BEFORE_UPDATE_REQUEST = 60;       // seconds before checking for new tag again (0 = INFINITE)
    public static final int         AREA_FACTOR_WIDTH           = 150;      // In percents
    public static final int         AREA_FACTOR_HEIGHT          = 150;      // In percents
    public static final double      MINIMUM_AREA_WIDTH          = 0.001;    // minimum area width in degrees
    public static final double      MINIMUM_AREA_HEIGHT         = 0.001;    // minimum area width in degrees

    // ---------------------------------------------------------------------------------------------

    public int                                      areaWidth;                     // En degré (1 degré ~= 100 km)
    public int                                      areaHeight;                                          // En degré
    public HashMap<IntPoint, AreaRequestItemInterface<T>>    areas;
    private IntLatLng                               center;
    private AreaDataLoaderInterface                 dataLoader = null;
    private AreaRequestItemFactory<T>               areaRequestItemFactory = null;
    private OnDataChangeListener                    onDataChangeListener;

    // ---------------------------------------------------------------------------------------------

    public AreaRequestHistory(final AreaDataLoaderInterface dataLoader) {
        this.areas = new HashMap<>();
        this.dataLoader = dataLoader;
        this.areaRequestItemFactory = new AreaRequestItemFactory<T>() {
            @Override
            public RAMAreaRequestItem<T> build() {
                return new RAMAreaRequestItem<>();
            }
        };
    }

    public OnDataChangeListener getOnDataChangeListener() {
        return onDataChangeListener;
    }

    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener) {
        this.onDataChangeListener = onDataChangeListener;
    }
    public void setAreaRequestItemFactory(AreaRequestItemFactory<T> factory){
        this.areaRequestItemFactory = factory;
    }

    public void resizeArea(LatLngBounds bounds) {
        Log.d(TAG, "Resizing area request history base area: " + bounds);
        clearAll();
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
     * From the remote_id point in cache, convert it as a bounds
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
    private void update(IntPoint p, AreaRequestItemInterface<T> item){
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
        Log.i(TAG, "Southwest=" + southwest.toString() + ". Northeast=" + northeast.toString() + " (in cache: " + areas.size() + ")");

        AreaIterator iterator = new AreaIterator(southwest, northeast);
        return iterator;
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

        // TODO Stef: find where bug come from
        if (areaIterator.size() > 100){ // TODO parameters
            Util.appStateError(TAG, "Area a size is hudge: " + areaIterator.size() + ". Something looks wrong... \n\t - Bounds are: " + bounds);
            return;
        }

        IntPoint p;
        while ((p = areaIterator.next()) != null){
            IntPoint pCpy = new IntPoint(p);
            AreaRequestItemInterface<T> request = this.areas.get(p);
            if (request != null){
                if (this.DELAY_BEFORE_UPDATE_REQUEST > 0 && request.getLastUpdateDelay() >= this.DELAY_BEFORE_UPDATE_REQUEST){
                    QueryCondition conditions = new QueryCondition();
                    Log.i(TAG, "-> " + p + " Data in cache are too old; updating with new data from dataTimestamp: " + request.getDataTimestamp());
                    conditions.setBounds(this.getBoundFromPoint(p).toDouble());
                    //conditions.setTimestampMin(request.dataTimestamp);
                    request.updateLocalTimestamp();

                    // Clear old ones
                    dataLoader.load(pCpy, request, conditions);
                }
                else{
                    Log.i(TAG, "-> " + p + " Data in cache are up to date (last update: " +request.getLastUpdateDelay()+ " seconds)");
                }
            }
            else{
                Log.i(TAG, "-> " + p + "  No data in cache; we need a server request");
                request = areaRequestItemFactory.build();
                this.update(pCpy, request);
                // We need to build a new condition object because multi threading
                QueryCondition conditions = new QueryCondition();
                conditions.setBounds(this.getBoundFromPoint(p).toDouble());
                request.updateLocalTimestamp();
                dataLoader.load(pCpy, request, conditions);
            }
        }
    }

    public void clearTooFarAreas(LatLngBounds bounds){
        IntPoint southeastPoint = getIntPoint(bounds.southwest);

        Iterator it = areas.entrySet().iterator();
        int distance;
        while (it.hasNext()) {
            Map.Entry<IntPoint, RAMAreaRequestItem<T>> entry = (Map.Entry) it.next();
            distance = entry.getKey().distance(southeastPoint);
            RAMAreaRequestItem<T> item = entry.getValue();

            if (distance > MAXIMUM_ORIGIN_DISTANCE) {
                item.clear();
                it.remove();
            }
        }
    }


    public IntLatLng getCenter() {
        return center;
    }

    public void clearAll() {
        Log.d(TAG, "clearAll() areas: " + this.areas.size());
        Iterator it = areas.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<IntPoint, RAMAreaRequestItem<T>> entry = (Map.Entry) it.next();
            RAMAreaRequestItem<T> item = entry.getValue();
            item.cancel();
            item.clear();
            it.remove();
        }
    }


    /**
     * Get only item inside the bounds
     */
    public List<T> getInsideBoundsItems(LatLngBounds bounds){
        Log.d(TAG, "AreaRequestHistory::getInsideBoundsItems(): " + bounds);
        LinkedList<T> result = new LinkedList<>();
        for (AreaRequestItemInterface<T> request:
             this.areas.values()) {
            if (request != null){
                for (T marker: request.getData()){
                    if (bounds.contains(marker.getPosition())){
                        result.add(marker);
                    }
                }
            }
        }
        return result;
    }

    public boolean isInitialized() {
        return center != null;
    }
}
