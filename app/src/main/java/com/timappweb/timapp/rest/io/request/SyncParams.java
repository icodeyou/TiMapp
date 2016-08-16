package com.timappweb.timapp.rest.io.request;

import android.os.Bundle;

import com.timappweb.timapp.sync.DataSyncAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stephane on 9/12/2015.
 */
public class SyncParams {

    public enum SyncDirection {DOWN, UP}

    public static final String SYNC_PARAM_MIN_CREATED       = "min_created";
    public static final String SYNC_PARAM_MAX_CREATED       = "max_created";
    public static final String SYNC_PARAM_ORDER             = "order";
    public static final String SYNC_PARAM_LIMIT             = "limit";
    public static final String SYNC_PARAM_LAST_UPDATE       = "last_update";
    public static final String SYNC_PARAM_DIRECTION         = "direction";
    public static final String SYNC_PARAM_MAX_ID            = "max_id";
    public static final String SYNC_PARAM_MIN_ID            = "min_id";

    // ---------------------------------------------------------------------------------------------

    private Bundle bundle;

    // ---------------------------------------------------------------------------------------------

    public SyncParams() {
        bundle = new Bundle();
    }

    public SyncParams(Bundle extras) {
        this.fromBundle(extras);
    }


    public HashMap<String, String> toMap() {
        HashMap<String, String> queryMap = new HashMap();
        if (bundle.containsKey(SYNC_PARAM_MIN_CREATED))
            queryMap.put(SYNC_PARAM_MIN_CREATED, String.valueOf(bundle.getLong(SYNC_PARAM_MIN_CREATED)));

        if (bundle.containsKey(SYNC_PARAM_MAX_CREATED))
            queryMap.put(SYNC_PARAM_MAX_CREATED, String.valueOf(bundle.getLong(SYNC_PARAM_MAX_CREATED)));

        if (bundle.containsKey(SYNC_PARAM_LIMIT))
            queryMap.put(SYNC_PARAM_LIMIT, String.valueOf(bundle.getInt(SYNC_PARAM_LIMIT)));

        if (bundle.containsKey(SYNC_PARAM_ORDER))
            queryMap.put(SYNC_PARAM_ORDER, String.valueOf(bundle.getInt(SYNC_PARAM_ORDER)));

        if (bundle.containsKey(SYNC_PARAM_LAST_UPDATE))
            queryMap.put(SYNC_PARAM_LAST_UPDATE, String.valueOf(bundle.getInt(SYNC_PARAM_LAST_UPDATE)));

        if (bundle.containsKey(SYNC_PARAM_MAX_ID))
            queryMap.put(SYNC_PARAM_MAX_ID, String.valueOf(bundle.getLong(SYNC_PARAM_MAX_ID)));

        if (bundle.containsKey(SYNC_PARAM_MIN_ID))
            queryMap.put(SYNC_PARAM_MIN_ID, String.valueOf(bundle.getLong(SYNC_PARAM_MIN_ID)));

        if (bundle.containsKey(SYNC_PARAM_DIRECTION))
            queryMap.put(SYNC_PARAM_DIRECTION, String.valueOf(bundle.getInt(SYNC_PARAM_DIRECTION)));

        return queryMap;
    }

    public Bundle toBundle() {
        return bundle;
    }

    @Override
    public String toString() {
        String res = "QueryCondition{" +
                "bundle= " + bundle +
                "}";
        return res;
    }

    // ---------------------------------------------------------------------------------------------

    public Bundle getBundle() {
        return bundle;
    }

    public SyncParams setMaxId(long id) {
        bundle.putLong(SYNC_PARAM_MAX_ID, id);
        return this;
    }
    public SyncParams setMinId(long id) {
        bundle.putLong(SYNC_PARAM_MIN_ID, id);
        return this;
    }

    public SyncParams setType(int type) {
        bundle.putInt(DataSyncAdapter.SYNC_TYPE_KEY, type);
        return this;
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
    public void fromBundle(Bundle params) {
        this.bundle = params;
    }

    public void setMinCreated(long minCreated) {
        bundle.putLong(SYNC_PARAM_MIN_CREATED, minCreated);
    }

    public void setMaxCreated(long maxCreated) {
        bundle.putLong(SYNC_PARAM_MAX_CREATED, maxCreated);
    }

    public long getMinCreated() {
        return bundle.getLong(SYNC_PARAM_MIN_CREATED);
    }

    public long getMaxCreated() {
        return Long.valueOf(bundle.getLong(SYNC_PARAM_MAX_CREATED));
    }

    public int getLimit() {
        return Integer.valueOf(bundle.getInt(SYNC_PARAM_LIMIT));
    }

    public boolean hasMinCreated() {
        return bundle.containsKey(SYNC_PARAM_MAX_CREATED);
    }

    public boolean hasMaxCreated() {
        return bundle.containsKey(SYNC_PARAM_MAX_CREATED);
    }

    public boolean hasLimit() {
        return bundle.containsKey(SYNC_PARAM_LIMIT);
    }

    public SyncParams setLimit(int limit) {
        bundle.putInt(SYNC_PARAM_LIMIT, limit);
        return this;
    }

    public SyncParams setLastUpdate(long lastUpdate) {
        bundle.putLong(SYNC_PARAM_LAST_UPDATE, lastUpdate);
        return this;
    }

    public SyncParams setDirection(SyncDirection direction) {
        bundle.putInt(SYNC_PARAM_DIRECTION, direction.ordinal());
        return this;
    }
}
