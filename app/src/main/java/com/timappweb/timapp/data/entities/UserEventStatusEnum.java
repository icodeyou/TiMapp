package com.timappweb.timapp.data.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Created by stephane on 1/29/2016.
 */
public enum UserEventStatusEnum {
    @SerializedName("coming")
    COMING,
    @SerializedName("here")
    HERE,
    @SerializedName("invited")
    INVITED,
    status, @SerializedName("gone")
    GONE //TODO Steph : Expliquer à Jack : un utilisateur GONE n'est plus HERE, et donc plus affiché dans la liste de gens ???
    ;

}
