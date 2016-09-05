package com.timappweb.timapp.rest.io.request;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.entities.SearchFilter;
import com.timappweb.timapp.data.models.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stephane on 9/12/2015.
 */
public class RestQueryParams {

    public enum SyncDirection {DOWN, UP}

    public static final String SYNC_PARAM_MIN_CREATED       = "min_created";
    public static final String SYNC_PARAM_MAX_CREATED       = "max_created";
    public static final String SYNC_PARAM_ORDER             = "order";
    public static final String SYNC_PARAM_LIMIT             = "limit";
    public static final String SYNC_PARAM_LAST_UPDATE       = "last_update";
    public static final String SYNC_PARAM_DIRECTION         = "direction";
    public static final String SYNC_PARAM_MAX_ID            = "max_id";
    public static final String SYNC_PARAM_MIN_ID            = "min_id";
    public static final String SYNC_PARAM_HASH_ID           = "hash";

    private HashMap<String, String> queryMap = new HashMap<>();

    public HashMap<String, String> toMap() {
        return this.queryMap;
    }

    /**
     * Set the view port
     * @param bounds
     */
    public RestQueryParams setBounds(LatLngBounds bounds) {
        queryMap.put("lat_ne", String.valueOf(bounds.northeast.latitude));
        queryMap.put("lat_sw", String.valueOf(bounds.southwest.latitude));
        queryMap.put("lon_ne", String.valueOf(bounds.northeast.longitude));
        queryMap.put("lon_sw", String.valueOf(bounds.southwest.longitude));
        queryMap.put("bounds", bounds.southwest.latitude + "," + bounds.southwest.longitude + "," + bounds.northeast.latitude + "," + bounds.northeast.longitude);
        return this;
    }

    public RestQueryParams setUserLocation(LatLng latLng){
        queryMap.put("latitude", String.valueOf(latLng.latitude));
        queryMap.put("longitude", String.valueOf(latLng.longitude));
        return this;
    }

    @Override
    public String toString() {
        String res = "RestQueryParams{\n";
        for (Map.Entry<String, String> entry : queryMap.entrySet()){
            res += entry.getKey() + ":" + entry.getValue() + " | ";
        }
        return res;
    }

    public void setTimeRange(int timeRange) {
        this.queryMap.put("time_range", String.valueOf(timeRange));
    }

    public void setUserLocation(double latitude, double longitude) {
        queryMap.put("latitude", String.valueOf(latitude));
        queryMap.put("longitude", String.valueOf(longitude));
    }

    public void setMainTags(boolean b) {
        queryMap.put("main_tags", b ? "1" : "0");
    }

    public void setPlaceId(int placeId) {
        queryMap.put("place_id", String.valueOf(placeId));
    }

    public void setAnonymous(boolean anonymous) {
        queryMap.put("anonymous", anonymous ? "1" : "0");
    }

    public void setUserLocation(Location lastLocation) {
        if (lastLocation == null) return;
        queryMap.put("latitude", String.valueOf(lastLocation.getLatitude()));
        queryMap.put("longitude", String.valueOf(lastLocation.getLongitude()));
    }

    public void setFilter(SearchFilter filter) {
        queryMap.put("filter_tags", Tag.tagsToString(filter.tags));
        queryMap.put("filter_categories", EventCategory.idsToString(filter.categories));
    }

    // ---------------------------------------------------------------------------------------------
    // Pagination options
    public RestQueryParams setLimit(long limit){
        return add(SYNC_PARAM_LIMIT, limit);
    }
    public RestQueryParams setMinId(long id){
        return add(SYNC_PARAM_MIN_ID, id);
    }
    public RestQueryParams setMaxId(long id){
        return add(SYNC_PARAM_MAX_ID, id);
    }

    // ---------------------------------------------------------------------------------------------
    // Add custom options

    public RestQueryParams add(String key, String value) {
        queryMap.put(key, value);
        return this;
    }
    public RestQueryParams add(String key, long value) {
        queryMap.put(key, String.valueOf(value));
        return this;
    }
    public RestQueryParams add(String key, int value) {
        queryMap.put(key, String.valueOf(value));
        return this;
    }
    public RestQueryParams add(String key, boolean value) {
        queryMap.put(key, String.valueOf(value));
        return this;
    }
    public RestQueryParams add(String key, double value) {
        queryMap.put(key, String.valueOf(value));
        return this;
    }


}
