package com.timappweb.timapp.rest;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.timappweb.timapp.data.models.MyModel;

/**
 * Created by Stephane on 15/08/2016.
 *
 * Save status for a sync
 */
@Table(name="SyncStatus")
public class SyncStatus extends MyModel {

    public enum SyncStatusType {PENDING, DONE, FAIL, SKIPPED}

    // ---------------------------------------------------------------------------------------------

    @Column(name = "TableName", notNull = true, uniqueGroups = "uniqSyncPerTableRange")
    private String table;

    @Column(name = "MinCreated", notNull = true, uniqueGroups = "uniqSyncPerTableRange")
    private long minCreated;

    @Column(name = "MaxCreated", notNull = true, uniqueGroups = "uniqSyncPerTableRange")
    private long maxCreated;

    @Column(name = "LastUpdate")
    private long lastUpdate;

    @Column(name = "Status", notNull = true)
    private SyncStatusType status;

    // ---------------------------------------------------------------------------------------------

    private SyncStatus() {

    }

    // ---------------------------------------------------------------------------------------------

    public SyncStatusType getStatus() {
        return status;
    }

    public long getMaxCreated() {
        return maxCreated;
    }

    // ---------------------------------------------------------------------------------------------

    public static SyncStatus create(String table, long minCreated, long maxCreated, SyncStatusType status) {
        SyncStatus syncStatus = new SyncStatus();
        syncStatus.table = table;
        syncStatus.minCreated = minCreated;
        syncStatus.maxCreated = maxCreated;
        syncStatus.status = status;
        syncStatus.lastUpdate = 0;
        return syncStatus;
    }

    public static SyncStatus getByMaxCreated(long created) {
        return new Select().from(SyncStatus.class).where("MaxCreated = ?", created).executeSingle();
    }


}
