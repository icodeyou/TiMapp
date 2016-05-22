package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

/**
 * Created by stephane on 5/8/2016.
 */
@Table(name = "EventTag")
public class EventTag extends MyModel {

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
                "event=" + event +
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
}
