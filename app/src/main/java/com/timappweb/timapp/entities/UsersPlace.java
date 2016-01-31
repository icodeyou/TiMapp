package com.timappweb.timapp.entities;

/**
 * Created by stephane on 1/29/2016.
 */
public class UsersPlace {

    public int place_id;
    public UserPlaceStatus status;
    public int user_id;
    public int created;

    @Override
    public String toString() {
        return "UsersPlace{" +
                "place_id=" + place_id +
                ", status=" + status +
                ", user_id=" + user_id +
                ", created=" + created +
                '}';
    }
}
