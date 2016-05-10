package com.timappweb.timapp.data.models;

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
public class SyncHistory extends Model {

    //@Column(name = "User", uniqueGroups = "uniqueHistoryPerUser")
    //User user;

    //@Column(name = "Type", uniqueGroups = "uniqueHistoryPerUser")
    @Column(name = "Type", unique = true)
    int type;

    @Column(name = "LastUpdate")
    long last_update;

    @Column(name = "Extra")
    String extra;

    // =============================================================================================

    public static void updateSync(int type){
        SyncHistory history = getByType(type);
        if (history == null){
            history = new SyncHistory();
            history.type = type;
        }
        history.last_update = System.currentTimeMillis();
        history.save();
    }


    public static boolean requireUpdate(int type, long updateMinDelay){
        SyncHistory history = getByType(type);
        return history == null || (System.currentTimeMillis() - history.last_update > updateMinDelay);
    }

    public static SyncHistory getByType(int type){
        return new Select()
                .from(SyncHistory.class)
                .where("Type = ?", type)
                .executeSingle();
    }

    public static long getLastSyncTime(int syncType) {
        SyncHistory history = getByType(syncType);
        return history != null ? history.last_update : 0;
    }
}
