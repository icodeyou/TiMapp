package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.adapters.EventUsersAdapter;
import com.timappweb.timapp.data.entities.PlaceUserInterface;
import com.timappweb.timapp.data.entities.PlacesInvitationStatus;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.utils.Util;

import java.util.List;

/**
 * Created by stephane on 3/28/2016.
 */
@Table(name = "PlacesInvitation")
public class PlacesInvitation extends SyncBaseModel implements PlaceUserInterface {

    @Column(name = "SyncId")
    @Expose
    public int id;

    @Expose
    public int user_id;

    @Expose
    public int target_id;

    @Column(name = "Created")
    @Expose(serialize = false, deserialize = true)
    public int created;

    @Column(name = "Modified")
    @Expose(serialize = false, deserialize = true)
    public int modified;

    @Column(name = "Status")
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

    // =============================================================================================

    @Override
    public long getSyncKey() {
        return this.id;
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        if (model == null) return false;
        if (!(model instanceof PlacesInvitation)) return false;
        PlacesInvitation invite = (PlacesInvitation) model;

        return this.status == invite.status
                && this.created == invite.created
                && this.modified == invite.modified;
    }
}
