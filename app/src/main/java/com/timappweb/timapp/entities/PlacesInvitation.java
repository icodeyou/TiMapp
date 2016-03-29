package com.timappweb.timapp.entities;

import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.utils.Util;

import java.util.List;

/**
 * Created by stephane on 3/28/2016.
 */
public class PlacesInvitation implements PlaceUserInterface {

    public int id;
    public int user_id;
    public int target_id;
    public int created;
    public int modified;
    public PlacesInvitationStatus status;

    @SerializedName("place")
    public Place place;

    @SerializedName("user_source")
    public User user_source;

    @SerializedName("user_target")
    public User user_target;

    @Override
    public List<Tag> getTags() {
        return null;
    }

    @Override
    public String getTimeCreated() {
        return Util.secondsTimestampToPrettyTime(((long) this.created) * 1000);
    }

    @Override
    public User getUser() {
        return this.user_target;
    }
}
