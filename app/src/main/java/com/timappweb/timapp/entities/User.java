package com.timappweb.timapp.entities;

import com.timappweb.timapp.utils.Util;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class User implements Serializable, PlaceUserInterface {
    private static final String TAG = "UserEntity" ;

    public int id = -1;
    public String username;
    public int age;
    public String password;
    public String email;
    public int photoId;
    public int count_posts = 0;
    public int count_places = 0;
    public SocialProvider provider;
    public String provider_uid;
    private boolean status = false;
    public LinkedList<Post> posts;

    public static final String KEY_NAME = "user.name";
    public static final String KEY_ID = "user.id";
    public static final String KEY_EMAIL = "user.email";
    public List<Tag> tags;

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
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", id=" + id +
                ", provider_uid=" + provider +
                '}';
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getProfilePictureUrl() {
        return "https://graph.facebook.com/" + this.provider_uid + "/picture?type=large";
    }

    @Override
    public List<Tag> getTags() {
        return null;
    }

    @Override
    public String getPostTime() {
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
        if (this.tags == null || this.tags.size() == 0){
            return "";
        }
        String res = tags.get(0).getName();

        for (int i = 1; i < tags.size(); i++){
            res += "," + tags.get(i).name;
        }
        return res;
    }
}
