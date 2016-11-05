package com.timappweb.timapp.data.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.Table;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.entities.UserEventStatusEnum;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

/**
 * Join table for
 */
@Table(database = AppDatabase.class)
public class UserEvent extends SyncBaseModel  {

    // =============================================================================================
    // DATABASE

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @NotNull
    @ForeignKey(tableClass = Event.class, onUpdate = ForeignKeyAction.CASCADE, onDelete = ForeignKeyAction.CASCADE)
    @Expose
    @SerializedName("place")
    public Event event;

    @NotNull
    @Column
    @Expose
    public UserEventStatusEnum status;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @ForeignKey(tableClass = User.class, onUpdate = ForeignKeyAction.CASCADE, onDelete = ForeignKeyAction.CASCADE)
    @NotNull
    @Expose
    @SerializedName("user")
    public User user;

    // =============================================================================================
    // Fields

    // =============================================================================================


    public UserEvent() {
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "event=" + event +
                ", status=" + status +
                ", user=" + user +
                ", created=" + created +
                '}';
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        if (model == null || !(model instanceof UserEvent)) return false;
        UserEvent obj = (UserEvent) model;
        return this.status == obj.status;
    }

    @Override
    public int getSyncType() {
        throw new InternalError("Not syncable");
    }

}
