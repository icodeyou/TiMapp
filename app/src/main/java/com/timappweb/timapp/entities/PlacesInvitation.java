package com.timappweb.timapp.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Created by stephane on 3/28/2016.
 */
public class PlacesInvitation {

    public int id;
    public int user_id;
    public int target_id;
    public int created;
    public int modified;
    public PlacesInvitationStatus status;

    //@SerializedName("User")
    //public User user;

    @SerializedName("Place")
    public Place place;
}
