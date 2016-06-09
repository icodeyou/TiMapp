package com.timappweb.timapp.data.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

import java.util.List;

/**
 * Created by stephane on 5/5/2016.
 */
@Table(name = "UserFriend")
public class UserFriend extends MyModel {

    // =============================================================================================
    // DATABASE

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "UserSource", uniqueGroups = "unique_friendship",
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    public User userSource;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @Column(name = "UserTarget", uniqueGroups = "unique_friendship",
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete= Column.ForeignKeyAction.CASCADE)
    public User userTarget;

    // =============================================================================================

    public UserFriend() {
    }

    public UserFriend(User userSource, User userTarget) {
        this.userSource = userSource;
        this.userTarget = userTarget;
    }

    @Override
    public String toString() {
        return "UserFriend{" +
                "userSource=" + userSource +
                ", userTarget=" + userTarget +
                '}';
    }

}