package com.timappweb.timapp.data.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.geometry.Bounds;
import com.timappweb.timapp.data.queries.AreaQueryHelper;

/**
 * Created by stephane on 5/8/2016.
 *
 * Sync history for bounds data
 */
@Table(name = "SyncHistoryBounds")
public class SyncHistoryBounds extends MyModel {

    private static final String TAG = "SyncHistory";

    // ---------------------------------------------------------------------------------------------

    /**
     * Sync type
     */
    @Column(name = "Type", notNull = true)
    int type;

    /**
     * last update (unix timestamp)
     */
    @Column(name = "LastUpdate", notNull = true)
    long last_update;

    /**
     * Bounds
     */
    @Column(name = "LatitudeSW", notNull = true)
    double latitude_sw;

    @Column(name = "LongitudeSW", notNull = true)
    double longitude_sw;

    @Column(name = "LatitudeNE", notNull = true)
    double latitude_ne;

    @Column(name = "LongitudeNE", notNull = true)
    double longitude_ne;

    // ---------------------------------------------------------------------------------------------

    /**
     * Set the current unix time for the specified sync time
     * @param type
     */
    public static void updateSync(int type, LatLngBounds bounds){
        Log.d(TAG, "Updating sync date for type=" + type);
        SyncHistoryBounds history = getByType(type, bounds);
        if (history == null){
            history = new SyncHistoryBounds();
            history.type = type;
            history.latitude_ne = bounds.northeast.latitude;
            history.longitude_ne = bounds.northeast.longitude;
            history.latitude_sw = bounds.southwest.latitude;
            history.longitude_sw = bounds.southwest.longitude;
        }
        history.last_update = System.currentTimeMillis();
        history.mySaveSafeCall();
    }

    /**
     *
     * @param type
     * @param updateMinDelay if 0 means infinite delay
     * @return
     */
    public static boolean requireUpdate(int type, LatLngBounds bounds, long updateMinDelay){
        SyncHistoryBounds history = getByType(type, bounds);
        return history == null || (updateMinDelay != 0 && (System.currentTimeMillis() - history.last_update) > updateMinDelay);
    }

    /**
     *
     * @param type
     * @return
     */
    public static SyncHistoryBounds getByType(int type, LatLngBounds bounds){
        return new Select()
                .from(SyncHistoryBounds.class)
                .where("Type = ? AND "
                                + AreaQueryHelper.locationInSQLBounds(bounds.southwest.latitude, bounds.southwest.longitude)
                                + " AND " + AreaQueryHelper.locationInSQLBounds(bounds.northeast.latitude, bounds.northeast.longitude),
                        type)
                .orderBy("LastUpdate DESC")
                .executeSingle();
    }

    /**
     *
     * @param syncType
     * @return
     */
    public static long getLastSyncTime(int syncType) {
        SyncHistoryBounds history = getByType(syncType, null);
        return history != null ? history.last_update : 0;
    }
}
