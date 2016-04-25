package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.utils.Util;

/**
 * Created by stephane on 4/5/2016.
 */
@Table(name = "PlaceStatus")
public class PlaceStatus extends SyncBaseModel {

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

    public static boolean hasStatus(int place_id, UserPlaceStatusEnum status) {
        PlaceStatus placeStatus = new Select()
                .from(PlaceStatus.class)
                .where("Status = ?", status)
                .where("PlaceId = ?", place_id)
                // .where("PlaceId = ?", place_id) TODO set created limit
                .executeSingle();
        return placeStatus != null;
    }

    public static PlaceStatus addStatus(int place_id, UserPlaceStatusEnum status){
        PlaceStatus placeStatus = new PlaceStatus(place_id, status);
        placeStatus.save();
        return placeStatus;
    }


    public long getSyncKey(){
        return this.getId();
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        if (!(model instanceof PlaceStatus)) return false;
        PlaceStatus that = (PlaceStatus) model;

        if (status != that.status) return false;
        return place_id != that.place_id;
    }

}
