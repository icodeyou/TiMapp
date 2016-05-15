package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.utils.Util;

/**
 * Created by stephane on 4/5/2016.
 */
@Table(name = "PlaceStatus")
public class PlaceStatus extends SyncBaseModel {

    private long MAX_STATUS_VALIDITY = 10800 * 1000; // Status validity is 3 hours

    // =============================================================================================
    // Database

    @ModelAssociation(type = ModelAssociation.Type.BELONGS_TO, joinModel = User.class)
    @Expose(serialize = false, deserialize = false)
    @Column(name = "User", index = true, notNull = true, onDelete = Column.ForeignKeyAction.CASCADE, onUpdate = Column.ForeignKeyAction.CASCADE)
    public User user;

    @ModelAssociation(type = ModelAssociation.Type.BELONGS_TO, joinModel = Place.class)
    @Expose
    @Column(name = "Place", index = true, notNull = true, onDelete = Column.ForeignKeyAction.CASCADE, onUpdate = Column.ForeignKeyAction.CASCADE)
    public Place place;

    @Expose
    @Column(name = "Created", notNull = true)
    public int created;

    @Expose
    @Column(name = "Status", notNull = true)
    public UserPlaceStatusEnum status;

    // =============================================================================================

    public PlaceStatus() {
        super();
    }

    public PlaceStatus(int place_id, UserPlaceStatusEnum status) {
        this.place = Place.loadByRemoteId(Place.class, place_id);
        this.status = status;
        this.created = Util.getCurrentTimeSec();
    }


    public PlaceStatus(User user, Place place, UserPlaceStatusEnum status) {
        this.user = user;
        this.place = place;
        this.status = status;
        this.created = Util.getCurrentTimeSec();
    }


    public static boolean hasStatus(Long userId, long placeId, UserPlaceStatusEnum status) {
        PlaceStatus placeStatus = getStatus(placeId, userId);
        if (placeStatus != null && !placeStatus.isStatusUpToDate()){
            placeStatus.delete();
            return false;
        }
        return placeStatus != null && placeStatus.status == status;
    }
    public static PlaceStatus getStatus(long placeId, long userId){
        return new Select()
                .from(PlaceStatus.class)
                .where("User = ? AND Place = ?", userId, placeId)
                .executeSingle();
    }
    public static PlaceStatus setStatus(User user, Place place, UserPlaceStatusEnum status, int remoteId){

        // Remove all other here status
        if (status == UserPlaceStatusEnum.HERE){
            new Delete().from(PlaceStatus.class).where("User = ? AND Status = ?", user.getId(), status).execute();
        }

        PlaceStatus placeStatus = getStatus(place.getId(), user.getId());
        if (placeStatus == null){
            placeStatus = new PlaceStatus(user, place, status);
        }
        else{
            placeStatus.status = status;
        }
        placeStatus.remote_id = remoteId;
        place.setRemoteId(remoteId);
        placeStatus.mySave();
        return placeStatus;
    }


    @Override
    public boolean isSync(SyncBaseModel model) {
        if (!(model instanceof PlaceStatus)) return false;
        PlaceStatus that = (PlaceStatus) model;

        if (status != that.status) return false;
        return place != that.place;
    }

    public boolean isStatusUpToDate() {
        return (this.created - System.currentTimeMillis()) < MAX_STATUS_VALIDITY;
    }

    public static void removeStatus(User user, Place place, UserPlaceStatusEnum status) {
        new Delete().from(PlaceStatus.class).where("User = ? AND Place = ?", user.getId(), place.getId()).execute();
    }
}
