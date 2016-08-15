package com.timappweb.timapp.rest.io.request;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stephane on 9/12/2015.
 */
public class SyncParams {

    private HashMap<String, String> queryMap = new HashMap<>();

    public static final String SYNC_PARAM_MIN_CREATED       = "min_created";
    public static final String SYNC_PARAM_MAX_CREATED       = "max_created";
    public static final String SYNC_PARAM_ORDER             = "order";
    public static final String SYNC_PARAM_LIMIT             = "limit";
    public static final String SYNC_PARAM_TYPE              = "last_update";

    public HashMap<String, String> toMap() {
        return this.queryMap;
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

    /**
     * min_created
     * max_created
     * order
     * limit
     * type
     *
     * @param params
     */
    public void setSyncParams(Bundle params) {
        queryMap.put(SYNC_PARAM_MIN_CREATED, params.getString(SYNC_PARAM_MIN_CREATED));
        queryMap.put(SYNC_PARAM_MAX_CREATED, params.getString(SYNC_PARAM_MAX_CREATED));
        queryMap.put(SYNC_PARAM_LIMIT, params.getString(SYNC_PARAM_LIMIT));
        queryMap.put(SYNC_PARAM_ORDER, params.getString(SYNC_PARAM_ORDER));
        queryMap.put(SYNC_PARAM_TYPE, params.getString(SYNC_PARAM_TYPE));
    }
    public void setMinCreated(long minCreated) {
        queryMap.put(SYNC_PARAM_MIN_CREATED, String.valueOf(minCreated));
    }
    public void setMaxCreated(long maxCreated) {
        queryMap.put(SYNC_PARAM_MAX_CREATED, String.valueOf(maxCreated));
    }


    public long getMinCreated() {
        return Long.valueOf(queryMap.get(SYNC_PARAM_MIN_CREATED));
    }

    public long getMaxCreated() {
        return Long.valueOf(queryMap.get(SYNC_PARAM_MAX_CREATED));
    }
    public int getLimit() {
        return Integer.valueOf(queryMap.get(SYNC_PARAM_LIMIT));
    }

    public boolean hasMinCreated() {
        return queryMap.containsKey(SYNC_PARAM_MAX_CREATED);
    }
    public boolean hasMaxCreated() {
        return queryMap.containsKey(SYNC_PARAM_MAX_CREATED);
    }

    public boolean hasLimit() {
        return queryMap.containsKey(SYNC_PARAM_LIMIT);
    }

    public void setLimit(int limit) {
        queryMap.put(SYNC_PARAM_LIMIT, String.valueOf(limit));
    }

    public void setLastUpdate(long lastUpdate) {
        queryMap.put(SYNC_PARAM_TYPE, String.valueOf(lastUpdate));
    }
}
