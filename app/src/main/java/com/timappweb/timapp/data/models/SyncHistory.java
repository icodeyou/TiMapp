package com.timappweb.timapp.data.models;

import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.timappweb.timapp.data.AppDatabase;

/**
 * Created by stephane on 5/8/2016.
 */
@Table(database = AppDatabase.class, uniqueColumnGroups = {
        @UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.REPLACE)
})
public class SyncHistory extends LocalModel {
    private static final String TAG = "SyncHistory";

    //@Column(name = "User", uniqueGroups = "uniqueHistoryPerUser")
    //User user;

    /**
     * Sync type
     */
    @Column
    @Unique(unique = false, uniqueGroups = 1)
    @NotNull
    int type;

    /**
     * last update (unix timestamp)
     */
    @Column
    @NotNull
    long last_update;

    /**
     * Extra identifier for the sync
     */
    @Column
    @Unique(unique = false, uniqueGroups = 1)
    @NotNull
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
     *  Update last sync time for the given type and object
     * @param type
     */
    public static void updateSync(int type, HistoryItemInterface object){
        Log.d(TAG, "Updating update sync date for type=" + type);
        String extraId = object != null ? object.hashHistoryKey() : null;
        SyncHistory history = getByType(type, extraId);
        if (history == null){
            history = new SyncHistory();
            history.type = type;
            history.extraID = extraId;
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
    public static boolean requireUpdate(int type, HistoryItemInterface object, long updateMinDelay){
        return updateMinDelay != 0 && ((System.currentTimeMillis() - getLastSyncTime(type, object)) > updateMinDelay);
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
        From<SyncHistory> from = SQLite.select()
                .from(SyncHistory.class);
        if (extraID != null){
            from
                    .where(SyncHistory_Table.type.eq(type))
                    .and(SyncHistory_Table.extraID.eq(extraID));
        }
        else{
            from
                .where(SyncHistory_Table.type.eq(type))
                .and(SyncHistory_Table.extraID.isNull());
        }
        return from.querySingle();
    }

    /**
     *
     * @param syncType
     * @return
     */
    public static long getLastSyncTime(int syncType) {
        return getLastSyncTime(syncType, null);
    }

    public static long getLastSyncTime(int syncType, HistoryItemInterface object) {
        SyncHistory history = getByType(syncType, object != null ? object.hashHistoryKey() : null);
        return history != null ? history.last_update : 0;
    }

    // ---------------------------------------------------------------------------------------------

    public interface HistoryItemInterface{
        String hashHistoryKey();
    }

}
