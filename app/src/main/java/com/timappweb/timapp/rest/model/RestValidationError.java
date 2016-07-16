package com.timappweb.timapp.rest.model;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by stephane on 9/12/2015.
 */
public class RestValidationError extends RestResponse {

    @Expose
    @SerializedName("returnCode")
    public int code;

    @Expose
    @SerializedName("message")
    public String message;

    @Expose
    @SerializedName("validationErrors")
    public JsonObject errors;

    // =============================================================================================

    public String toString(){
        return "RestValidationError[code: "+code+"; message=" + message + ";]";
    }

    public RestValidationErrorParser getErrors(){
        return new RestValidationErrorParser(this.errors);
    }
}
