package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.adapters.EventUsersAdapter;
import com.timappweb.timapp.data.entities.PlaceUserInterface;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.utils.Util;

import java.util.List;

@Table(name = "UserPlace")
public class UserPlace implements PlaceUserInterface {

    @Column(name = "PlaceId")
    @Expose
    public int place_id;

    @Column(name = "Status")
    @Expose
    public UserPlaceStatusEnum status;

    @Column(name = "UserId")
    @Expose
    public int user_id;

    @Column(name = "Created")
    @Expose
    public int created;

    @Expose
    @SerializedName("user")
    public User user;

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
    public int getViewType() {
        if(status== UserPlaceStatusEnum.HERE) {
            return EventUsersAdapter.VIEW_TYPES.HERE;
        } else if(status== UserPlaceStatusEnum.COMING){
            return EventUsersAdapter.VIEW_TYPES.COMING;
        } else if(status== UserPlaceStatusEnum.INVITED){
            return EventUsersAdapter.VIEW_TYPES.INVITED;
        } else {
            return EventUsersAdapter.VIEW_TYPES.UNDEFINED;
        }
    }
}
