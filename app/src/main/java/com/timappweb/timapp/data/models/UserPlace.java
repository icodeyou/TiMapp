package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.data.entities.PlaceUserInterface;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.utils.Util;

import java.util.List;

@Table(name = "UserPlace")
public class UserPlace extends SyncBaseModel implements PlaceUserInterface {

    // =============================================================================================
    // DATABASE

    @Column(name = "PlaceId")
    @Expose
    public int place_id;

    @Column(name = "Status")
    @Expose
    public UserPlaceStatusEnum status;

    @Column(name = "Created")
    @Expose
    public int created;

    @Column(name = "User")
    @Expose
    @SerializedName("user")
    public User user;

    // =============================================================================================
    // Fields

    @Expose(deserialize = false, serialize = true)
    public int user_id;

    // =============================================================================================

    @Override
    public String toString() {
        return "UserPlace{" +
                "place_id=" + place_id +
                ", status=" + status +
                ", user_id=" + user_id + " (" + user + ")" +
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
}
