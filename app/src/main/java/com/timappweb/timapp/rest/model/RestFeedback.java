package com.timappweb.timapp.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Created by stephane on 9/12/2015.
 */
public class RestFeedback extends RestResponse {


    @SerializedName("success")
    public boolean success;

    @SerializedName("returnCode")
    public int code;

    @SerializedName("comment")
    public String message;

    @SerializedName("steps")
    public String steps;
    public HashMap<String, String> data;


    public String toString(){
        return "ServerObject[Success: " + success + "; message=" + message + ";]";
    }


}
