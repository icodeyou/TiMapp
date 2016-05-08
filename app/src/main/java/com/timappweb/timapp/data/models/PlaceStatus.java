package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.utils.Util;

/**
 * Created by stephane on 4/5/2016.
 */
@Table(name = "PlaceStatus")
public class PlaceStatus extends SyncBaseModel {

    private long MAX_STATUS_VALIDITY = 10600 * 1000; // Status validity is 3 hours

    @Expose(serialize = false, deserialize = false)
    @Column(name = "User", index = true, onDelete= Column.ForeignKeyAction.CASCADE)
    public User user;

    // TODO change as Place
    @Expose
    @Column(name = "PlaceId", index = true)
    public int place_id;

    @Expose
    @Column(name = "Created")
    public int created;

    @Expose
    @Column(name = "Status")
    public UserPlaceStatusEnum status;

    public PlaceStatus() {
        super();
    }

    public PlaceStatus(int place_id, UserPlaceStatusEnum status) {
        this.place_id = place_id;
        this.status = status;
        this.created = Util.getCurrentTimeSec();
    }

    public static boolean hasStatus(int placeId, UserPlaceStatusEnum status) {
        PlaceStatus placeStatus = new Select()
                .from(PlaceStatus.class)
                .where("Status = ? AND PlaceId = ?", status, placeId)
                .executeSingle();
        if (placeStatus != null && !placeStatus.isStatusUpToDate()){
            placeStatus.delete();
            return false;
        }
        return placeStatus != null;
    }

    public static PlaceStatus addStatus(int place_id, UserPlaceStatusEnum status){
        PlaceStatus placeStatus = new PlaceStatus(place_id, status);
        placeStatus.save();
        return placeStatus;
    }


    @Override
    public boolean isSync(SyncBaseModel model) {
        if (!(model instanceof PlaceStatus)) return false;
        PlaceStatus that = (PlaceStatus) model;

        if (status != that.status) return false;
        return place_id != that.place_id;
    }

    public boolean isStatusUpToDate() {
        return (this.created - System.currentTimeMillis()) < MAX_STATUS_VALIDITY;
    }
}
