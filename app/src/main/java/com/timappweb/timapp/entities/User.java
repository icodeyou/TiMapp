package com.timappweb.timapp.entities;

import com.timappweb.timapp.data.LocalPersistenceManager;

public class User {

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

    public String username;
    public String password;
    public String email;

    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";

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
