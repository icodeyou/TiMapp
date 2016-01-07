package com.timappweb.timapp.rest;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stephane on 9/12/2015.
 */
public class QueryCondition {

    private Map<String, String> queryMap = new HashMap<>();

    // TODO remove the getter
    public Map<String, String> toMap() {
        return this.queryMap;
    }

    /**
     * Set the view port
     * @param bounds
     */
    public void setBounds(LatLngBounds bounds) {
        queryMap.put("lat_ne", String.valueOf(bounds.northeast.latitude));
        queryMap.put("lat_sw", String.valueOf(bounds.southwest.latitude));
        queryMap.put("lon_ne", String.valueOf(bounds.northeast.longitude));
        queryMap.put("lon_sw", String.valueOf(bounds.southwest.longitude));
    }

    public void setUserLocation(LatLng latLng){
        queryMap.put("latitude", String.valueOf(latLng.latitude));
        queryMap.put("longitude", String.valueOf(latLng.longitude));
    }

    public void setTimestampMin(int min) {
        if (min > 0)
            queryMap.put("ts_min", String.valueOf(min));
        // queryMap.put("cache[]", dataTimestamp + "," + bounds.northeast.latitude + "," + bounds.southwest.longitude + "," +
        //       "" + bounds.southwest.latitude + "," + bounds.southwest.longitude);
    }
    public void setTimestampMax(int max) {
        queryMap.put("ts_min", String.valueOf(max));
    }

    @Override
    public String toString() {
        String res = "QueryCondition{\n";
        for (Map.Entry<String, String> entry : queryMap.entrySet()){
            res += entry.getKey() + ":" + entry.getValue() + " | ";
        }
        return res;
    }

    public void setVisualisation(String val) {
        this.queryMap.put("visualization", val);
    }

    public void setTimeRange(int timeRange) {
        this.queryMap.put("time_range", String.valueOf(timeRange));
    }

    public void setUserLocation(double latitude, double longitude) {
        queryMap.put("latitude", String.valueOf(latitude));
        queryMap.put("longitude", String.valueOf(longitude));
    }
}
