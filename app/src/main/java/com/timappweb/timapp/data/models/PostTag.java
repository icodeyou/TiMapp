package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

/**
 * Created by stephane on 6/6/2016.
 */
public class PostTag extends MyModel {

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "EventPost", uniqueGroups = "unique_tag",
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    public EventPost eventPost;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "Tag", uniqueGroups = "unique_tag",
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    public Tag tag;

    // ---------------------------------------------------------------------------------------------

    public PostTag(EventPost eventPost, Tag tag) {
        this.eventPost = eventPost;
        this.tag = tag;
    }

    public PostTag(Tag tag, EventPost eventPost) {
        this.eventPost = eventPost;
        this.tag = tag;
    }


}
