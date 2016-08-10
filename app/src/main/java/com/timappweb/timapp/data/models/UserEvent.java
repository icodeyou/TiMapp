package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.data.entities.PlaceUserInterface;
import com.timappweb.timapp.data.entities.UserEventStatusEnum;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.utils.Util;

import java.util.List;

/**
 * Join table for
 */
@Table(name = "UserEvent")
public class UserEvent extends SyncBaseModel implements PlaceUserInterface {

    private long MAX_STATUS_VALIDITY = 10800 * 1000; // Status validity is 3 hours

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

    @Column(name = "Created", notNull = true)
    @Expose
    public int created;

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

    public UserEvent(int place_id, UserEventStatusEnum status) {
        this.event = Event.loadByRemoteId(Event.class, place_id);
        this.status = status;
        this.created = Util.getCurrentTimeSec();
    }
    public UserEvent(User user, Event event, UserEventStatusEnum status) {
        this.user = user;
        this.event = event;
        this.status = status;
        this.created = Util.getCurrentTimeSec();
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

    @Override
    public List<Tag> getTags() {
        return null;
    }

    @Override
    public String getTimeCreated() {
        return Util.secondsTimestampToPrettyTime(this.created);
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        if (model == null || !(model instanceof UserEvent)) return false;
        UserEvent obj = (UserEvent) model;
        return this.status == obj.status;
    }

    public static From queryForPlace(Event event) {
        return queryForPlace(event.getId());
    }

    public static From queryForPlace(long placeId) {
        return new Select().from(UserEvent.class).where("Event = ?", placeId);
    }

    public static List<UserEvent> getForPlace(Event event) {
        return queryForPlace(event).execute();
    }
    /*
    public static From queryForPlace(long id) {
        return new Select().from(UserEvent.class).where("Event = ?", id);
    }*/

    // ---------------------------------------------------------------------------------------------
    public static boolean hasStatus(Long userId, long placeId, UserEventStatusEnum status) {
        UserEvent eventStatus = getStatus(placeId, userId);
        if (eventStatus != null && !eventStatus.isStatusUpToDate()){
            eventStatus.delete();
            return false;
        }
        return eventStatus != null && eventStatus.status == status;
    }
    public static UserEvent getStatus(long placeId, long userId){
        return new Select()
                .from(UserEvent.class)
                .where("User = ? AND Event = ?", userId, placeId)
                .orderBy("Created DESC")
                .executeSingle();
    }
    public static UserEvent setStatus(User user, Event event, UserEventStatusEnum status, int remoteId){

        // Remove all other here status
        if (status == UserEventStatusEnum.HERE){
            new Delete().from(UserEvent.class).where("User = ? AND Status = ?", user.getId(), status).execute();
        }

        UserEvent eventStatus = getStatus(event.getId(), user.getId());
        if (eventStatus == null){
            eventStatus = new UserEvent(user, event, status);
        }
        else{
            eventStatus.status = status;
        }
        eventStatus.remote_id = remoteId;
        eventStatus.mySave();
        return eventStatus;
    }


    public boolean isStatusUpToDate() {
        return (this.created - System.currentTimeMillis()) < MAX_STATUS_VALIDITY;
    }

    public static void removeStatus(User user, Event event, UserEventStatusEnum status) {
        new Delete().from(UserEvent.class).where("User = ? AND Event = ?", user.getId(), event.getId()).execute();
    }
}
