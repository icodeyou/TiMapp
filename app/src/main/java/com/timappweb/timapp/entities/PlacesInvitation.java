package com.timappweb.timapp.entities;

import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.adapters.PlaceUsersAdapter;
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

    public PlacesInvitation(Place place, User user) {
        this.place = place;
        this.user_target = user;
    }

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
        return Util.secondsTimestampToPrettyTime((this.created));
    }

    @Override
    public User getUser() {
        return this.user_target;
    }

    @Override
    public int getViewType() {
        return PlaceUsersAdapter.VIEW_TYPES.INVITED;
    }

    public User getUserSource() {
        return this.user_source;
    }
}
