package com.timappweb.timapp.entities;

import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.data.LocalPersistenceManager;

import java.io.Serializable;
import java.util.LinkedList;

public class User implements Serializable {
    public String username;
    public String password;
    public String email;
    public int count_posts = 0;
    public int user_id;


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
    }

    public void writeToPref() {
        LocalPersistenceManager.instance.editor.putString(KEY_NAME, this.username);
        LocalPersistenceManager.instance.editor.putString(KEY_EMAIL, this.email);
    }
}
