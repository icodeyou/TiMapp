package com.timappweb.timapp.rest.model;

import android.app.Activity;

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

    @SerializedName("message")
    public String message;

    @SerializedName("data")
    public HashMap<String, String> data;



    public String toString(){
        return "ServerObject[Success: " + success + " ("+code+"); message=" + message + ";]";
    }


}
