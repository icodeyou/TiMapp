package com.timappweb.timapp.data.loader.paginate;

import com.google.gson.JsonObject;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.models.MyModel;

/**
 * Created by Stephane on 03/11/2016.
 */

@Table(database = AppDatabase.class)
public class CacheInfo extends MyModel {

    public      int         limit = 10;
    public      int         updateLimit = 20;

    /**
     * Sync group key
     */
    @Column
    @NotNull
    @PrimaryKey
    public String cacheId;

    @Column
    protected   long        lastUpdate = -1;

    @Column
    protected   String      updateUrl;

    @Column
    protected   String      nextUrl;

    @Column
    protected   String      initialUrl;

    @Column
    protected   String      prevUrl;

    @Column
    protected   long      expireDate = 0;

    @Column
    protected   int      total = -1;

    public CacheInfo() {}

    public boolean isValid(){
        return expireDate == 0 || (System.currentTimeMillis() < expireDate);
    }

    public void updateInfo(CursorPaginateDataLoader.ResponseWrapper<JsonObject> feedback, CursorPaginateDataLoader.LoadType loadType) {
        if (loadType == CursorPaginateDataLoader.LoadType.NEXT) this.nextUrl = feedback.getNextUrl();
        if (loadType == CursorPaginateDataLoader.LoadType.PREV) this.prevUrl = feedback.getPrevUrl();
        this.updateUrl = feedback.getUpdateUrl();
        if (loadType == CursorPaginateDataLoader.LoadType.UPDATE || this.lastUpdate == -1) this.lastUpdate = feedback.time;
        this.updateUrl = feedback.getUpdateUrl();
        this.total = feedback.total;
    }

    @Override
    public String toString() {
        return "CacheInfo{" +
                "limit=" + limit +
                ", cacheId='" + cacheId + '\'' +
                ", lastUpdate=" + lastUpdate +
                ", updateUrl='" + updateUrl + '\'' +
                ", nextUrl='" + nextUrl + '\'' +
                ", initialUrl='" + initialUrl + '\'' +
                ", prevUrl='" + prevUrl + '\'' +
                '}';
    }

    public void reset() {
        nextUrl = this.initialUrl;
        lastUpdate = -1;
    }

    @Override
    public void deleteAssociation(Class<? extends MyModel> associationModel, Property property) {
        // Nothing
    }
}