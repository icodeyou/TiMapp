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


    public boolean isStatusUpToDate() {
        return (this.created - System.currentTimeMillis()) < MAX_STATUS_VALIDITY;
    }

    public static boolean hasStatus(Long userId, long placeId, UserEventStatusEnum status) {
        UserEvent eventStatus = getStatus(placeId, userId);
        if (eventStatus != null && !eventStatus.isStatusUpToDate()){
            eventStatus.delete();
            return false;
        }
        return eventStatus != null && eventStatus.status == status;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Get user status for a particular event
     * @param eventId
     * @param userId
     * @return
     */
    public static UserEvent getStatus(long eventId, long userId){
        return new Select()
                .from(UserEvent.class)
                .where("User = ? AND Event = ?", userId, eventId)
                .orderBy("SyncId DESC")
                .executeSingle();
    }

    public static void removeStatus(User user, Event event, UserEventStatusEnum status) {
        new Delete().from(UserEvent.class).where("User = ? AND Event = ?", user.getId(), event.getId()).execute();
    }

    /**
     * Get the current HERE event for current user
     * If there is a gone status after the here, it means that it's not the current event anymore
     * @return
     */
    public static UserEvent getCurrentEventStatus() {
        UserEvent lastHereStatus = new Select()
                .from(UserEvent.class)
                .where("(Status = ? OR Status = ?) AND User = ?", UserEventStatusEnum.HERE, UserEventStatusEnum.GONE, MyApplication.getCurrentUser().getId())
                .orderBy("SyncId DESC")
                .executeSingle();
        if (lastHereStatus != null && lastHereStatus.status == UserEventStatusEnum.GONE){
            return null;
        }
        return lastHereStatus;
    }

}
