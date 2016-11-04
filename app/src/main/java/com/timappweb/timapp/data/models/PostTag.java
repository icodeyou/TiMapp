package com.timappweb.timapp.data.models;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

/**
 * Created by stephane on 6/6/2016.
 */
@Table(database = AppDatabase.class, uniqueColumnGroups = {
    @UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.IGNORE)
})
public class PostTag extends LocalModel {

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @ForeignKey(tableClass = EventPost.class, onDelete = ForeignKeyAction.CASCADE, onUpdate = ForeignKeyAction.CASCADE)
    @Unique(unique = false, uniqueGroups = 1)
    public EventPost event_post;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @ForeignKey(tableClass = Tag.class, onDelete = ForeignKeyAction.CASCADE, onUpdate = ForeignKeyAction.CASCADE)
    @Unique(unique = false, uniqueGroups = 1)
    public Tag tag;

    // ---------------------------------------------------------------------------------------------


    public PostTag() {
    }

    public PostTag(EventPost eventPost, Tag tag) {
        this.event_post = eventPost;
        this.tag = tag;
    }

    public PostTag(Tag tag, EventPost eventPost) {
        this.event_post = eventPost;
        this.tag = tag;
    }


}
