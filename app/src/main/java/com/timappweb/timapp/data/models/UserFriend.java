package com.timappweb.timapp.data.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
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
 * Created by stephane on 5/5/2016.
 */
@Table(database = AppDatabase.class,
    uniqueColumnGroups = {
            @UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.IGNORE)
    }
)
public class UserFriend extends SyncBaseModel {

    // =============================================================================================
    // DATABASE

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @NotNull
    @ForeignKey(tableClass = User.class, onUpdate = ForeignKeyAction.CASCADE, onDelete = ForeignKeyAction.CASCADE)
    @Unique(unique = false, uniqueGroups = 1)
    @SerializedName("user_source")
    public User user_source;

    @ModelAssociation(joinModel = User.class, type = ModelAssociation.Type.BELONGS_TO)
    @NotNull
    @ForeignKey(tableClass = User.class, onUpdate = ForeignKeyAction.CASCADE, onDelete = ForeignKeyAction.CASCADE)
    @Unique(unique = false, uniqueGroups = 1)
    @SerializedName("user_target")
    @Expose
    public User user_target;

    // =============================================================================================

    public UserFriend() {
    }

    public UserFriend(User userSource, User userTarget) {
        this.user_source = userSource;
        this.user_target = userTarget;
    }

    @Override
    public String toString() {
        return "UserFriend{" +
                "user_source=" + user_source +
                ", user_target=" + user_target +
                '}';
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        return false;
    }

    @Override
    public int getSyncType() {
        throw new InternalError("Not syncable");
    }
}
