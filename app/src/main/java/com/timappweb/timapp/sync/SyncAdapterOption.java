package com.timappweb.timapp.sync;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.data.models.SyncHistory;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.utils.Util;

import java.util.HashMap;

/**
 * Created by stephane on 5/12/2016.
 */
public class SyncAdapterOption implements SyncHistory.HistoryItemInterface, Cloneable {

    private static final String TAG = "SyncAdapterOption";

    @Override
    public String hashHistoryKey() {
        return getHashId();
    }

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

    private Bundle bundle;

    public SyncAdapterOption(int type) {
        bundle = new Bundle();
        setType(type);
    }

    public SyncAdapterOption setType(int type){
        bundle.putInt(DataSyncAdapter.SYNC_TYPE_KEY, type);
        return this;
    }

    public SyncAdapterOption() {
        bundle = new Bundle();
    }

    public SyncAdapterOption(Bundle extras) {
        this.bundle = extras;
    }

    public int getSyncType() {
        return bundle.getInt(DataSyncAdapter.SYNC_TYPE_KEY);
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setLastSyncTime() {
        bundle.putLong(DataSyncAdapter.LAST_SYNC_TIME, SyncHistory.getLastSyncTime(getSyncType()));
    }

    public void set(String key, LatLngBounds bounds) {
        bundle.putDouble(key + "swlatitude", bounds.southwest.latitude);
        bundle.putDouble(key + "swlongitude", bounds.southwest.longitude);
        bundle.putDouble(key + "nelatitude", bounds.northeast.latitude);
        bundle.putDouble(key + "nelongitude", bounds.northeast.longitude);
    }


    // ---------------------------------------------------------------------------------------------




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
            queryMap.put(SYNC_PARAM_LAST_UPDATE, String.valueOf(bundle.getLong(SYNC_PARAM_LAST_UPDATE)));

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

    public SyncAdapterOption setMaxId(long id) {
        bundle.putLong(SYNC_PARAM_MAX_ID, id);
        return this;
    }
    public SyncAdapterOption setMinId(long id) {
        bundle.putLong(SYNC_PARAM_MIN_ID, id);
        return this;
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

    public String getHashId() {
        return bundle.getString(SYNC_PARAM_HASH_ID, null);
    }

    public SyncAdapterOption setHashId(SyncHistory.HistoryItemInterface object) {
        bundle.putString(SYNC_PARAM_HASH_ID, object.hashHistoryKey());
        return this;
    }

    public SyncAdapterOption setLimit(int limit) {
        bundle.putInt(SYNC_PARAM_LIMIT, limit);
        return this;
    }

    public SyncAdapterOption setLastUpdate(long lastUpdate) {
        bundle.putLong(SYNC_PARAM_LAST_UPDATE, lastUpdate);
        return this;
    }

    public SyncAdapterOption setDirection(SyncDirection direction) {
        bundle.putInt(SYNC_PARAM_DIRECTION, direction.ordinal());
        return this;
    }


    @Override
    public SyncAdapterOption clone() {
        try {
            return (SyncAdapterOption) super.clone();
        } catch (CloneNotSupportedException e) {
            Util.appStateError(TAG, e.getMessage());
        }
        return this;
    }
}
