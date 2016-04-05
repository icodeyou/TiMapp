package com.timappweb.timapp.database.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

/**
 * Created by stephane on 4/5/2016.
 */
public class User extends Model{

    @Column(name = "Id", index = true)
    public int id;

    @Column(name = "Username", index = true)
    public String username;

    @Column(name = "Token")
    public String token;

    @Column(name = "SocialToken")
    public String social_token;

}
