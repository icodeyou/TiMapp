package com.timappweb.timapp.entities;

import com.timappweb.timapp.utils.Util;

import java.util.List;

public class UsersPlace implements PlaceUserInterface{

    public int place_id;
    public UserPlaceStatus status;
    public int user_id;
    public int created;

    public User user;

    @Override
    public String toString() {
        return "UsersPlace{" +
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
    public String getPostTime() {
        return Util.secondsTimestampToPrettyTime(this.created);
    }

    @Override
    public User getUser() {
        return user;
    }
}
