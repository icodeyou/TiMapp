package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.data.entities.PlaceUserInterface;
import com.timappweb.timapp.data.entities.PlacesInvitationStatus;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.utils.Util;

import java.util.List;

/**
 * Created by stephane on 3/28/2016.
 */
@Table(name = "PlacesInvitation")
public class PlacesInvitation extends SyncBaseModel implements PlaceUserInterface {

    // =============================================================================================
    // Database

    @Column(name = "Created", notNull = true)
    @Expose(serialize = false, deserialize = true)
    public int created;

    @Column(name = "Modified")
    @Expose(serialize = false, deserialize = true)
    public int modified;

    @Column(name = "Status", notNull = true)
    @Expose
    public PlacesInvitationStatus status;

    @ModelAssociation(joinModel = Place.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "Place",
            notNull = true,
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    @Expose
    @SerializedName("place")
    public Place place;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "UserSource",
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    @Expose
    @SerializedName("user_source")
    public User user_source;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "UserTarget",
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    @Expose
    @SerializedName("user_target")
    public User user_target;

    // =============================================================================================

    public PlacesInvitation() {}

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

    public User getUserSource() {
        return this.user_source;
    }

    @Override
    public List<Tag> getTags() {
        return null;
    }

    @Override
    public String toString() {
        return "PlacesInvitation{" +
                "created=" + created +
                ", status=" + status +
                ", place=" + place +
                ", user_source=" + user_source +
                ", user_target=" + user_target +
                '}';
    }

// =============================================================================================


    @Override
    public boolean isSync(SyncBaseModel model) {
        if (model == null) return false;
        if (!(model instanceof PlacesInvitation)) return false;
        PlacesInvitation invite = (PlacesInvitation) model;

        return this.status == invite.status
                && this.created == invite.created
                && this.modified == invite.modified
                && this.user_target == invite.user_target
                && this.user_source == invite.user_source
                && this.place == invite.place;
    }
}
