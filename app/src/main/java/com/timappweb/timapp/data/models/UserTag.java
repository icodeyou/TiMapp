package com.timappweb.timapp.data.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

/**
 * Created by stephane on 5/8/2016.
 */
@Table(name = "UserTag")
public class UserTag extends MyModel {

    // =============================================================================================
    // DATABASE
    @ModelAssociation(type = ModelAssociation.Type.BELONGS_TO, joinModel = User.class)
    @Column(name = "User", uniqueGroups = "unique_tag",
            notNull = true,
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    public User user;

    @ModelAssociation(type = ModelAssociation.Type.BELONGS_TO, joinModel = User.class)
    @Column(name = "Tag", uniqueGroups = "unique_tag",
            notNull = true,
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    public Tag tag;

    // =============================================================================================

    public UserTag() {}

    @Override
    public String toString() {
        return "UserFriend{" +
                "user=" + user +
                ", tag=" + tag +
                '}';
    }

    public UserTag(User user, Tag tag) {
        this.user = user;
        this.tag = tag;
    }
}
