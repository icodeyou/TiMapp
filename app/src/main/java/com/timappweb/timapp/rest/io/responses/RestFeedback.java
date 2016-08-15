package com.timappweb.timapp.rest.io.responses;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Created by stephane on 9/12/2015.
 */
public class RestFeedback extends RestResponse {

    @Expose
    @SerializedName("success")
    public boolean success;

    @Expose
    @SerializedName("returnCode")
    public int code;

    @Expose
    @SerializedName("message")
    public String message;

    @Expose
    @SerializedName("data")
    public HashMap<String, String> data;

    @Expose
    @SerializedName("validationErrors")
    public JsonObject errors;

    // =============================================================================================

    public String toString(){
        return "ServerObject[Success: " + success + " ("+code+"); message=" + message + ";]";
    }

    public int getIntData(String name) {
        try {
            return Integer.valueOf(this.data.get(name));
        } catch (NumberFormatException ex){
            return -1;
        }
    }

}
