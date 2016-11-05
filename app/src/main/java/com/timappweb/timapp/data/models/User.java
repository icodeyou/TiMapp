package com.timappweb.timapp.data.models;

import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.data.tables.UsersTable;
import com.timappweb.timapp.sync.data.DataSyncAdapter;

import java.util.List;

@Table(database = AppDatabase.class)
public class User extends SyncBaseModel  {
    private static final String TAG = "UserEntity" ;

    // =============================================================================================
    // DATABASE

    @Column
    @NotNull
    @Expose
    public String username;

    @Column
    @Expose
    public String email;

    @Column
    @Expose(serialize = true, deserialize = true)
    public Integer count_posts;

    @Column
    @Expose(serialize = true, deserialize = true)
    public Integer count_places;

    @Column
    @Expose
    public String avatar_url;

    //@Column
    //@Expose(serialize = true, deserialize = true)
    //private boolean status = false;

    @Column
    @Expose(serialize = true, deserialize = true)
    public String app_id;

    @Column
    @Expose(serialize = true, deserialize = true)
    public String google_messaging_token;

    // =============================================================================================

    /**
     * Cached value. See @getTags
     */
    @Expose(serialize = true, deserialize = true)
    @ModelAssociation(
            type = ModelAssociation.Type.BELONGS_TO_MANY,
            saveStrategy = ModelAssociation.SaveStrategy.REPLACE,
            joinModel = UserTag.class,
            targetModel = Tag.class,
            targetTable = UserTag_Table.class,
            remoteForeignKey = "user_id")
    public List<Tag> tags;

    /**
     * Cached value
     */
    protected List<UserEvent> placeStatus;

    // =============================================================================================

    public User(){

    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", avatar_url=" + avatar_url +
                ", app_id=" + app_id +
                ", google_messaging_token =" + google_messaging_token +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePictureUrl() {
        return this.avatar_url;
    }

    public boolean hasTags() {
        return tags != null && tags.size() > 0;
    }

    // =============================================================================================

    public List<Tag> getTags() {
        if (tags != null) return tags;
        tags = UsersTable.loadUserTags(this);
        return tags;
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        return false;
    }

    @Override
    public int getSyncType() {
        return DataSyncAdapter.SYNC_TYPE_USER;
    }

    public List<UserEvent> getPlaceStatus() {
        if (placeStatus != null) return placeStatus;
        placeStatus = SQLite.select()
                .from(UserEvent.class)
                .where(UserEvent_Table.user_id.eq(this.id))
                .queryList();
        return placeStatus;
    }


    public Where<EventsInvitation> getInviteSentQuery() {
        return SQLite.select()
                .from(EventsInvitation.class)
                .where(EventsInvitation_Table.user_source_id.eq(this.id));
    }

    public List<EventsInvitation> getInviteSent(long placeId) {
        return this.getInviteSentQuery().queryList();
    }

    public Where<EventsInvitation> getInviteReceivedQuery() {
        return SQLite.select()
                .from(EventsInvitation.class)
                .where(EventsInvitation_Table.user_target_id.eq(this.id))
                .orderBy(EventsInvitation_Table.id, false);
    }

    public List<EventsInvitation> getInviteReceived() {
        return this.getInviteReceivedQuery().queryList();
    }


    public UserQuota getQuota(int quotaTypeId) {
        return UserQuota.get(this.id, quotaTypeId);
    }

/*
    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "tags")
    public List<Ant> getMyTags() {
        if (tags == null || tags.isEmpty()) {
            tags = SQLite.select()
                    .from(Tag.class)
                    .where(Tag_Table..eq(id))
                    .queryList();
        }
        return ants;
    }
*/
    @Override
    public void deepSave() throws CannotSaveModelException {
        if (this.tags != null && tags.size() > 0){
            FlowManager.getDatabase(AppDatabase.class).executeTransaction(new ITransaction() {
                @Override
                public void execute(DatabaseWrapper databaseWrapper) {
                    SQLite.delete(UserTag.class)
                            .where(UserTag_Table.user_id.eq(User.this.id))
                            .execute();
                    for (Tag tag: tags){
                        new UserTag(User.this, tag).mySaveSafeCall();
                    }
                }
            });
        }
        this.mySave();
    }
}
