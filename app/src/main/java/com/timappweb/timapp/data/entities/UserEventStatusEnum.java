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
    @SerializedName("gone")
    GONE
    ;

}
