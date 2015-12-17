package com.timappweb.timapp.entities;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.data.LocalPersistenceManager;

import java.io.Serializable;
import java.util.LinkedList;

public class User implements Serializable {
    private static final String TAG = "UserEntity" ;
    public String username;
    public String password;
    public String email;
    public int count_posts = 0;
    public int id;
    public boolean status = false;


    @SerializedName("spots")
    public LinkedList<Post> posts;

    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";

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

    public void loadFromPref(){
        this.email = LocalPersistenceManager.instance.pref.getString(KEY_EMAIL, null);
        this.password = "";
        this.username = LocalPersistenceManager.instance.pref.getString(KEY_NAME, null);
        Log.d(TAG, "Loading user form pref: " + this);
    }

    public void writeToPref() {
        Log.d(TAG, "Writing user form pref: " + this);
        LocalPersistenceManager.instance.editor.putString(KEY_NAME, this.username);
        LocalPersistenceManager.instance.editor.putString(KEY_EMAIL, this.email);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", user_id=" + id +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public void setHere(boolean status) {
        this.status = status;
    }

}
