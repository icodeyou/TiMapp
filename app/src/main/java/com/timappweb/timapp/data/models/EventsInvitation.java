package com.timappweb.timapp.data.models;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.Table;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.entities.PlacesInvitationStatus;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.utils.Util;

@Table(database = AppDatabase.class)
public class EventsInvitation extends SyncBaseModel {

    // =============================================================================================
    // Database

    @Column
    @Expose(serialize = true, deserialize = true)
    public int modified;

    @Column(name = "Status")
    @Expose
    @NotNull
    public PlacesInvitationStatus status;

    @ModelAssociation(joinModel = Event.class, type = ModelAssociation.Type.BELONGS_TO)
    @ForeignKey(tableClass = Event.class,
            saveForeignKeyModel = true,
            onUpdate = ForeignKeyAction.CASCADE,
            onDelete = ForeignKeyAction.CASCADE)
    @NotNull
    @Expose
    @SerializedName("place")
    public Event event;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @ForeignKey(tableClass = User.class,
            saveForeignKeyModel = true,
            onUpdate = ForeignKeyAction.CASCADE,
            onDelete = ForeignKeyAction.CASCADE)
    @NotNull
    @Expose
    @SerializedName("user_source")
    public User user_source;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @ForeignKey(tableClass = User.class,
            saveForeignKeyModel = true,
            onUpdate = ForeignKeyAction.CASCADE,
            onDelete = ForeignKeyAction.CASCADE)
    @NotNull
    @Expose
    @SerializedName("user_target")
    public User user_target;

    // =============================================================================================

    public EventsInvitation() {}

    public EventsInvitation(Event event, User user) {
        this.event = event;
        this.user_target = user;
    }

    public String getTimeCreated() {
        Log.d("Debug pretty time", "Invitation created : "+created);
        return Util.secondsTimestampToPrettyTime(this.created);
    }

    public User getUser() {
        return this.user_target;
    }

    public User getUserSource() {
        return this.user_source;
    }

    @Override
    public String toString() {
        return "EventsInvitation{" +
                ", id=" + this.id +
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

    @Override
    public int getSyncType() {
        throw new InternalError("Not syncable: " + this);
    }
}
