package com.timappweb.timapp.database.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.utils.Util;

/**
 * Created by stephane on 4/5/2016.
 */
public class PlaceStatus extends Model {

    @Column(name = "PlaceId", index = true)
    public int place_id;

    @Column(name = "Created")
    public int created;

    @Column(name = "Status")
    public UserPlaceStatus status;

    public PlaceStatus() {
        super();
    }

    public PlaceStatus(int place_id, UserPlaceStatus status) {
        this.place_id = place_id;
        this.status = status;
        this.created = Util.getCurrentTimeSec();
    }

    public static boolean hasStatus(int place_id, UserPlaceStatus status) {
        PlaceStatus placeStatus = new Select()
                .from(QuotaType.class)
                .where("Status = ?", status)
                .where("PlaceId = ?", place_id)
                // .where("PlaceId = ?", place_id) TODO set created limit
                .executeSingle();
        return placeStatus != null;
    }
}
