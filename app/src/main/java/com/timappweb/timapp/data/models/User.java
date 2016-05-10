package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.adapters.EventUsersAdapter;
import com.timappweb.timapp.data.entities.PlaceUserInterface;
import com.timappweb.timapp.data.entities.SocialProvider;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Table(name = "User")
public class User extends SyncBaseModel implements Serializable, PlaceUserInterface {
    private static final String TAG = "UserEntity" ;

    // =============================================================================================
    // DATABASE

    @Column(name = "Username")
    @Expose
    public String username;

    @Column(name = "Email")
    @Expose
    public String email;

    @Column(name = "CountPosts")
    @Expose(serialize = false, deserialize = true)
    public int count_posts;

    @Column(name = "CountPlaces")
    @Expose(serialize = false, deserialize = true)
    public int count_places;

    @Column(name = "ProviderUID")
    @Expose
    public String provider_uid;

    @Column(name = "Provider")
    @Expose
    public SocialProvider provider;

    @Column(name = "Status")
    @Expose(serialize = false, deserialize = true)
    private boolean status = false;

    @Column(name = "AppId")
    @Expose(serialize = false, deserialize = true)
    public String app_id;

    @Column(name = "GoogleMessagingToken")
    @Expose(serialize = false, deserialize = true)
    public String google_messaging_token;

    // =============================================================================================

    @Expose(serialize = true, deserialize = false)
    public String password;

    /**
     * Cached value. See @getTags
     */
    @Expose(serialize = false, deserialize = true)
    @ModelAssociation(
            type = ModelAssociation.Type.BELONGS_TO_MANY,
            saveStrategy = ModelAssociation.SaveStrategy.REPLACE,
            joinModel = UserTag.class)
    protected List<Tag> tags;

    /**
     * Cached value
     */
    protected List<UserPlace> placeStatus;

    // =============================================================================================

    public List<User> getFriends() {
        return this.getFriendsQuery().execute();
    }

    public User(){

    }

    public User(String email, String password){
        this.email = email;
        this.password = password;
    }

    public User(String email, String password, String username){
        this.email = email;
        this.password = password;
        this.username = username;
    }

    private static int dummyIndice = 0;
    public static User createDummy(){
        User user = new User("user"+dummyIndice+"@dummy.com", "dummy", "Dummy User " + dummyIndice);
        dummyIndice++;
        return user;
    }

    @Override
    public String toString() {
        return "User{" +
                " db_id=" + this.getId() +
                ", remote_id=" + remote_id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", provider_uid=" + provider +
                ", app_id=" + app_id +
                ", google_messaging_token =" + google_messaging_token +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePictureUrl() {
        return "https://graph.facebook.com/" + this.provider_uid + "/picture?type=large";
    }

    @Override
    public List<Tag> getTags() {
        if (tags != null) return tags;
        tags = new Select().from(Tag.class).join(UserTag.class).on("Tag.Id = UserTag.Tag").execute();
        return tags;
    }
    public boolean hasTags() {
        return getTags().size() > 0;
    }

    @Override
    public String getTimeCreated() {
        return null;
    }

    @Override
    public User getUser() {
        return this;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }

    public String getTagsToString() {
        return Tag.tagsToString(this.tags);
    }

    public List<Tag> setNewbieTag() {
        return null;
    }

    // =============================================================================================

    @Override
    public boolean isSync(SyncBaseModel model) {
        return false;
    }


    public static From getFriendsQuery(long userId) {
        return new Select()
                .from(User.class)
                .innerJoin(UserFriend.class)
                .on("User.Id = UserFriend.UserTarget")
                .where("UserFriend.UserSource = ?", userId);
    }

    public List<UserPlace> getPlaceStatus() {
        if (placeStatus != null) return placeStatus;
        placeStatus = new Select()
                .from(UserPlace.class)
                .where("User = ? ", this.getId())
                .execute();
        return placeStatus;
    }


    public From getInviteSentQuery() {
        return new Select()
                .from(PlacesInvitation.class)
                .where("UserSource = ?", this.getId());
    }

    public From getInviteSentQuery(long placeId) {
        return this.getInviteSentQuery().where("Place = ?", placeId);
    }

    public From getInviteReceivedQuery() {
        return new Select()
                .from(PlacesInvitation.class)
                .where("UserTarget = ?", this.getId())
                .orderBy("created DESC");
    }

    public From getInviteReceivedQuery(long placeId) {
        return getInviteReceivedQuery().where("Place = ?", placeId);
    }

    public List<PlacesInvitation> getInviteReceived() {
        return this.getInviteReceivedQuery().execute();
    }

    public From getFriendsQuery() {
        return User.getFriendsQuery(this.getId());
    }


    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

}
