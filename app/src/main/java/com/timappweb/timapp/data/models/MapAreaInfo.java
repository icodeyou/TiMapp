package com.timappweb.timapp.data.models;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.data.queries.AreaQueryHelper;

/**
 * Created by stephane on 4/26/2016.
 *
 * Store information about area that are already loaded in the database
 *      - The map area loaded represented by south west and north east coordinates
 *      - The number of items loaded
 *      - The total number of items
 *      - The loading date (unix timestamp format <=> timezone independant)
 */
@Table(name = "MapAreaInfo")
public class MapAreaInfo extends Model {

    private static final String TAG = "MapAreaInfo";

    public static final int MAP_EVENT = 1;
    public static final int AROUND_USER = 2;

    /**
     * Area bounds
     */
    @Column(name="NELatitude")
    public double NE_latitude;

    @Column(name="NELongitude")
    public double NE_longitude;

    @Column(name="SWLatitude")
    public double SW_latitude;

    @Column(name="SWLongitude")
    public double SW_longitude;

    /**
     * Date of creation (timestamp) of the query
     */
    @Column(name="Created")
    public long created;

    /**
     * Total of result for this pagination
     */
    @Column(name="Total")
    public int total;

    /**
     * Number of element actually loaded
     */
    @Column(name="Size")
    public int size;

    /**
     * Type of element (Event, post ?)
     */
    @Column(name="DataType")
    public int data_type;

    // =============================================================================================

    public MapAreaInfo(LatLngBounds bounds, int type, int total, int size){
        this.NE_latitude = bounds.northeast.latitude;
        this.NE_longitude = bounds.northeast.longitude;
        this.SW_latitude = bounds.southwest.latitude;
        this.SW_longitude = bounds.southwest.longitude;
        this.data_type = type;
        this.total = total;
        this.size = size;
        this.created = System.currentTimeMillis();
    }

    // =============================================================================================

    public static void clearOldArea(int type, long timeago){
        Log.i(TAG, "Clearing old map areas for type=" + type + " more that " + (timeago / 1000) + " seconds old");
        new Delete()
                .from(MapAreaInfo.class)
                .where(" DataType = ? AND " + MapAreaInfo.oldCondition(timeago), type)
                .execute();
    }

    public static MapAreaInfo addNewArea(LatLngBounds bounds, int type, int total, int size){
        return MapAreaInfo.addNewArea(new MapAreaInfo(bounds, type, total, size));
    }

    public static MapAreaInfo addNewArea(MapAreaInfo data){
        Log.i(TAG, "Adding a new map area for type=" + data.data_type + " for bounds " + data.getBounds());
        ActiveAndroid.beginTransaction();

        data.created = System.currentTimeMillis();

        // Remove area that are included in this new area
        new Delete()
                .from(MapAreaInfo.class)
                .where(areaContainedInBounds(data.getBounds()))
                .execute();

        data.save();

        ActiveAndroid.endTransaction();

        return data;
    }
    /**
     *
     * @param bounds
     * @return true if data are in cache
     */
    public static boolean isInCache(LatLngBounds bounds, int type, long timeago){
        return findArea(bounds, type, timeago).exists();
    }

    public static boolean isInCache(LatLngBounds bounds, int type){
        return isInCache(bounds, type, 0);
    }

    private static String oldCondition(long timeago){
        return "Created >= " + (System.currentTimeMillis() - timeago);
    }

    public LatLngBounds getBounds() {
        return new LatLngBounds(new LatLng(this.SW_latitude, this.SW_longitude), new LatLng(this.NE_latitude, this.NE_latitude));
    }

    public static From findArea(LatLngBounds bounds, int type, long timeago) {
        String whereClause = "DataType = " + type
                + " AND " + areaContainedInBounds(bounds);
        if (timeago > 0){
            whereClause += " AND " + oldCondition(timeago);
        }
        return new Select()
                .from(MapAreaInfo.class)
                .where(whereClause);
    }
    public static From findArea(LatLngBounds bounds, int type) {
        return findArea(bounds, type, 0);
    }


    // =============================================================================================
    /**
     * Create a condition to get an area that contains the bounds area
     * @param bounds
     * @return
     */
    private static String areaContainedInBounds(LatLngBounds bounds){
        return areaContainedInBounds(bounds.southwest)
                + " AND " + areaContainedInBounds(bounds.northeast);
    }


    /**
     * Create a condition to get an area that contains the location
     * @param location
     * @return
     */
    private static String areaContainedInBounds(LatLng location){
        return latitudeCondition(location.latitude)
                + " AND " + longitudeCondition(location.longitude);
    }

    /**
     * Inspired by LatLngBounds.contain()
     * @param latitude
     * @return
     */
    private static String latitudeCondition(double latitude) {
        return "(SWLatitude <= " + latitude + " &&  " + latitude + "  <= NELatitude)";
    }

    /**
     * Inspired by LatLngBounds.contain()
     * @param longitude
     * @return
     */
    private static String longitudeCondition(double longitude) {
        return "(( SWLongitude <= NELongitude AND SWLongitude <= " + longitude + " AND " +
                longitude + " <= NELongitude) OR (SWLongitude <= " + longitude + " OR " + longitude + " <= NELongitude))";
    }

}
