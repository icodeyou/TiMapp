package com.timappweb.timapp.data.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

/**
 * Created by stephane on 5/8/2016.
 */
@Table(database = AppDatabase.class, uniqueColumnGroups = {@UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.IGNORE)})
public class EventTag extends LocalModel {
    private static final String TAG = "EventTag";

    // =============================================================================================
    // DATABASE

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @ForeignKey(tableClass = Event.class, onUpdate = ForeignKeyAction.CASCADE, onDelete = ForeignKeyAction.CASCADE)
    @Unique(unique = false, uniqueGroups = 1)
    @NotNull
    public Event event;

    @ModelAssociation(joinModel = Tag.class, type = ModelAssociation.Type.BELONGS_TO)
    @ForeignKey(tableClass = Tag.class, onUpdate = ForeignKeyAction.CASCADE, onDelete = ForeignKeyAction.CASCADE)
    @Unique(unique = false, uniqueGroups = 1)
    @NotNull
    public Tag tag;

    @Column
    @NotNull
    public int count_ref;


    // =============================================================================================
    // DATABASE

    @Override
    public String toString() {
        return "EventTag{" +
                "count_ref=" + count_ref +
                ", tag=" + tag +
                ", event=" + (event != null ? event.getName() + " (" + event.getRemoteId() + ") " : "NONE") +
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

    public String getTagName() {
        return tag != null ? tag.name : "";
    }

}
