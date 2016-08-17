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

@Table(name = "EventsInvitation")
public class EventsInvitation extends SyncBaseModel implements PlaceUserInterface {

    // =============================================================================================
    // Database

    @Column(name = "Modified")
    @Expose(serialize = false, deserialize = true)
    public int modified;

    @Column(name = "Status", notNull = true)
    @Expose
    public PlacesInvitationStatus status;

    @ModelAssociation(joinModel = Event.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "Event",
            notNull = true,
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    @Expose
    @SerializedName("place")
    public Event event;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "UserSource",
            notNull = true,
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    @Expose
    @SerializedName("user_source")
    public User user_source;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "UserTarget",
            notNull = true,
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    @Expose
    @SerializedName("user_target")
    public User user_target;

    // =============================================================================================

    public EventsInvitation() {}

    public EventsInvitation(Event event, User user) {
        this.event = event;
        this.user_target = user;
    }

    @Override
    public String getTimeCreated() {
        return Util.secondsTimestampToPrettyTime(this.created);
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
        return "EventsInvitation{" +
                "db_id=" + this.getId() +
                ", remote_id=" + this.remote_id +
                ", created=" + created +
                ", status=" + status +
                ", event=" + event +
                ", user_source=" + user_source +
                ", user_target=" + user_target +
                '}';
    }

// =============================================================================================


    @Override
    public boolean isSync(SyncBaseModel model) {
        if (model == null) return false;
        if (!(model instanceof EventsInvitation)) return false;
        EventsInvitation invite = (EventsInvitation) model;

        return this.status == invite.status
                && this.created == invite.created
                && this.modified == invite.modified
                && this.user_target == invite.user_target
                && this.user_source == invite.user_source
                && this.event == invite.event;
    }
}
