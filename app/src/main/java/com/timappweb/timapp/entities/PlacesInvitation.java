package com.timappweb.timapp.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.adapters.EventUsersAdapter;
import com.timappweb.timapp.utils.Util;

import java.util.List;

/**
 * Created by stephane on 3/28/2016.
 */
public class PlacesInvitation implements PlaceUserInterface {

    @Expose
    public int id;

    @Expose
    public int user_id;

    @Expose
    public int target_id;

    @Expose(serialize = false, deserialize = true)
    public int created;

    @Expose(serialize = false, deserialize = true)
    public int modified;

    @Expose
    public PlacesInvitationStatus status;

    @Expose
    @SerializedName("place")
    public Place place;

    @Expose
    @SerializedName("user_source")
    public User user_source;

    @Expose
    @SerializedName("user_target")
    public User user_target;


    // =============================================================================================

    public PlacesInvitation(Place place, User user) {
        this.place = place;
        this.user_target = user;
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
        return EventUsersAdapter.VIEW_TYPES.INVITED;
    }

    public User getUserSource() {
        return this.user_source;
    }

    @Override
    public List<Tag> getTags() {
        return null;
    }

}
