package com.timappweb.timapp.data.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.timappweb.timapp.MyApplication;

/**
 * Created by stephane on 5/8/2016.
 */
@Table(name = "SyncHistory")
public class SyncHistory extends MyModel {
    private static final String TAG = "SyncHistory";

    //@Column(name = "User", uniqueGroups = "uniqueHistoryPerUser")
    //User user;

    //@Column(name = "Type", uniqueGroups =
    /**
     * Sync type
     */
    @Column(name = "Type", unique = true, notNull = true)
    int type;

    /**
     * last update (unix timestamp)
     */
    @Column(name = "LastUpdate", notNull = true)
    long last_update;

    /**
     * Extra identifier for the sync
     */
    @Column(name = "ExtraID", notNull = false)
    String extraID;

    // =============================================================================================

    /**
     * Set the current unix time for the specified sync time
     * @param type
     */
    public static void updateSync(int type){
        updateSync(type, null);
    }


    /**
     *
     * @param type
     */
    public static void updateSync(int type, String extraID){
        Log.d(TAG, "Updating update sync date for type=" + type);
        SyncHistory history = getByType(type, extraID);
        if (history == null){
            history = new SyncHistory();
            history.type = type;
            history.extraID = extraID;
        }
        history.last_update = System.currentTimeMillis();
        history.mySave();
    }

    /**
     *
     * @param type
     * @param updateMinDelay if 0 means infinite delay
     * @return
     */
    public static boolean requireUpdate(int type, String extraID, long updateMinDelay){
        SyncHistory history = getByType(type, extraID);
        return history == null || (updateMinDelay != 0 && System.currentTimeMillis() - history.last_update > updateMinDelay);
    }

    /**
     *
     * @param type
     * @param updateMinDelay if 0 means infinite delay
     * @return
     */
    public static boolean requireUpdate(int type, long updateMinDelay){
        return requireUpdate(type, null, updateMinDelay);
    }

    /**
     *
     * @param type
     * @return
     */
    public static SyncHistory getByType(int type, String extraID){
        return new Select()
                .from(SyncHistory.class)
                .where("Type = ? AND ExtraId", type, extraID)
                .executeSingle();
    }

    /**
     *
     * @param syncType
     * @return
     */
    public static long getLastSyncTime(int syncType) {
        SyncHistory history = getByType(syncType, null);
        return history != null ? history.last_update : 0;
    }
}
