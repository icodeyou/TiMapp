package com.timappweb.timapp.data.models;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

import java.util.List;

/**
 * Created by stephane on 5/8/2016.
 */
@Table(name = "EventTag")
public class EventTag extends SyncBaseModel {

    // =============================================================================================
    // DATABASE

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "Event", uniqueGroups = "unique_tag",
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    public Event event;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "Tag", uniqueGroups = "unique_tag",
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    public Tag tag;

    @Column(name = "CountRef")
    public int count_ref;


    // =============================================================================================
    // DATABASE

    @Override
    public String toString() {
        return "EventTag{" +
                "count_ref=" + count_ref +
                ", event=" + event +
                ", tag=" + tag +
                '}';
    }

    public EventTag(Event event, Tag tag) {
        this.event = event;
        this.tag = tag;
    }

    public EventTag(Event event, Tag model, int count_ref) {
        this(event, model);
        this.count_ref = count_ref;
    }

    public EventTag() {}

    /**
     * Increment the tag counter by one for each given tag in the list
     * @param tags
     */
    public static void incrementCountRef(Event event, List<Tag> tags) {
        if (!event.hasLocalId()){
            throw new IllegalStateException();
        }
        ActiveAndroid.beginTransaction();
        for (Tag tag: tags){
            if (!tag.hasLocalId()) tag = (Tag) tag.mySave();
            EventTag existingEventTag = new Select().from(EventTag.class)
                    .where("EventTag.Tag = ? AND EventTag.Event = ?", tag.getId(), event.getId())
                    .executeSingle();
            // Insert if does not exists
            if (existingEventTag != null){
                existingEventTag.count_ref += 1;
                existingEventTag.mySave();
                //new Update(EventTag.class)
                //        .set("CountRef = CountRef + 1")
                //        .where("EventTag.Tag = ? AND EventTag.Event = ?", tag.getId(), event.getId())
                //        .execute();
            }
            // Update existing record
            else{
                new EventTag(event, tag, 1).mySave();
            }
        }
        ActiveAndroid.setTransactionSuccessful();
        ActiveAndroid.endTransaction();
    }

    public String getTagName() {
        return tag != null ? tag.name : "";
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        return false;
    }
}
