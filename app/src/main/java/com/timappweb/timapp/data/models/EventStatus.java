package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.utils.Util;

/**
 * Created by stephane on 4/5/2016.
 */
@Table(name = "EventStatus")
public class EventStatus extends SyncBaseModel {

    private long MAX_STATUS_VALIDITY = 10800 * 1000; // Status validity is 3 hours

    // =============================================================================================
    // Database

    @ModelAssociation(type = ModelAssociation.Type.BELONGS_TO, joinModel = User.class)
    @Expose(serialize = false, deserialize = false)
    @Column(name = "User", index = true, notNull = true, onDelete = Column.ForeignKeyAction.CASCADE, onUpdate = Column.ForeignKeyAction.CASCADE)
    public User user;

    @ModelAssociation(type = ModelAssociation.Type.BELONGS_TO, joinModel = Event.class)
    @Expose
    @Column(name = "Event", index = true, notNull = true, onDelete = Column.ForeignKeyAction.CASCADE, onUpdate = Column.ForeignKeyAction.CASCADE)
    @SerializedName("place")
    public Event event;

    @Expose
    @Column(name = "Created", notNull = true)
    public int created;

    @Expose
    @Column(name = "Status", notNull = true)
    public UserPlaceStatusEnum status;

    // =============================================================================================

    public EventStatus() {
        super();
    }

    public EventStatus(int place_id, UserPlaceStatusEnum status) {
        this.event = Event.loadByRemoteId(Event.class, place_id);
        this.status = status;
        this.created = Util.getCurrentTimeSec();
    }


    public EventStatus(User user, Event event, UserPlaceStatusEnum status) {
        this.user = user;
        this.event = event;
        this.status = status;
        this.created = Util.getCurrentTimeSec();
    }


    public static boolean hasStatus(Long userId, long placeId, UserPlaceStatusEnum status) {
        EventStatus eventStatus = getStatus(placeId, userId);
        if (eventStatus != null && !eventStatus.isStatusUpToDate()){
            eventStatus.delete();
            return false;
        }
        return eventStatus != null && eventStatus.status == status;
    }
    public static EventStatus getStatus(long placeId, long userId){
        return new Select()
                .from(EventStatus.class)
                .where("User = ? AND Event = ?", userId, placeId)
                .executeSingle();
    }
    public static EventStatus setStatus(User user, Event event, UserPlaceStatusEnum status, int remoteId){

        // Remove all other here status
        if (status == UserPlaceStatusEnum.HERE){
            new Delete().from(EventStatus.class).where("User = ? AND Status = ?", user.getId(), status).execute();
        }

        EventStatus eventStatus = getStatus(event.getId(), user.getId());
        if (eventStatus == null){
            eventStatus = new EventStatus(user, event, status);
        }
        else{
            eventStatus.status = status;
        }
        eventStatus.remote_id = remoteId;
        event.setRemoteId(remoteId);
        eventStatus.mySave();
        return eventStatus;
    }


    @Override
    public boolean isSync(SyncBaseModel model) {
        if (!(model instanceof EventStatus)) return false;
        EventStatus that = (EventStatus) model;

        if (status != that.status) return false;
        return event != that.event;
    }

    public boolean isStatusUpToDate() {
        return (this.created - System.currentTimeMillis()) < MAX_STATUS_VALIDITY;
    }

    public static void removeStatus(User user, Event event, UserPlaceStatusEnum status) {
        new Delete().from(EventStatus.class).where("User = ? AND Event = ?", user.getId(), event.getId()).execute();
    }
}
