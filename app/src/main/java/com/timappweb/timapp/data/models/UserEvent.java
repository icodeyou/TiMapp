package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.entities.UserEventStatusEnum;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

/**
 * Join table for
 */
@Table(name = "UserEvent")
public class UserEvent extends SyncBaseModel  {

    // =============================================================================================
    // DATABASE

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "Event", notNull = true, onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    @Expose
    @SerializedName("place")
    public Event event;

    @Column(name = "Status", notNull = true)
    @Expose
    public UserEventStatusEnum status;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "User", notNull = true, onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
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

}
