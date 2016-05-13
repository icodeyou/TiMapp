package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.data.entities.PlaceUserInterface;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.utils.Util;

import java.util.List;

@Table(name = "UserPlace")
public class UserPlace extends SyncBaseModel implements PlaceUserInterface {

    // =============================================================================================
    // DATABASE

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "Place", notNull = true, onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    @Expose
    public Place place;

    @Column(name = "Status", notNull = true)
    @Expose
    public UserPlaceStatusEnum status;

    @Column(name = "Created", notNull = true)
    @Expose
    public int created;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "User", notNull = true, onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    @Expose
    @SerializedName("user")
    public User user;

    // =============================================================================================
    // Fields

    // =============================================================================================


    public UserPlace() {
    }

    @Override
    public String toString() {
        return "UserPlace{" +
                "place=" + place +
                ", status=" + status +
                ", user=" + user +
                ", created=" + created +
                '}';
    }

    @Override
    public List<Tag> getTags() {
        return null;
    }

    @Override
    public String getTimeCreated() {
        return Util.secondsTimestampToPrettyTime(this.created);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        if (model == null || !(model instanceof UserPlace)) return false;
        UserPlace obj = (UserPlace) model;
        return this.status == obj.status;
    }

    public static From queryForPlace(Place place) {
        return queryForPlace(place.getId());
    }

    public static From queryForPlace(long placeId) {
        return new Select().from(UserPlace.class).where("Place = ?", placeId);
    }

    public static List<UserPlace> getForPlace(Place place) {
        return queryForPlace(place).execute();
    }
    /*
    public static From queryForPlace(long id) {
        return new Select().from(UserPlace.class).where("Place = ?", id);
    }*/
}
