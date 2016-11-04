package com.timappweb.timapp.data.models;

import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

/**
 * Created by stephane on 5/8/2016.
 */
@Table(database = AppDatabase.class, uniqueColumnGroups = {
        @UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.REPLACE)
})
public class UserTag extends LocalModel {

    // =============================================================================================
    // DATABASE
    @ModelAssociation(type = ModelAssociation.Type.BELONGS_TO, joinModel = User.class)
    @NotNull
    @ForeignKey(tableClass = User.class, onDelete = ForeignKeyAction.CASCADE, onUpdate = ForeignKeyAction.CASCADE)
    @Unique(unique = false, uniqueGroups = 1)
    public User user;

    @ModelAssociation(type = ModelAssociation.Type.BELONGS_TO, joinModel = Tag.class)
    @NotNull
    @ForeignKey(tableClass = Tag.class, onDelete = ForeignKeyAction.CASCADE, onUpdate = ForeignKeyAction.CASCADE)
    @Unique(unique = false, uniqueGroups = 1)
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
