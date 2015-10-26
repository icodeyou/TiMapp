package com.timappweb.timapp.rest.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by stephane on 9/12/2015.
 */
public class RestFeedback extends RestResponse {


    @SerializedName("success")
    public boolean success;

    @SerializedName("returnCode")
    public int code;

    @SerializedName("message")
    public String message;

    @SerializedName("steps")
    public String steps;


    public String toString(){
        return "ServerObject[Success: " + success + "; message=" + message + ";]";
    }


}
