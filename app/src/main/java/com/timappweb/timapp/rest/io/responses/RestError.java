package com.timappweb.timapp.rest.io.responses;

import com.google.gson.annotations.SerializedName;


/**
 * Created by stephane on 9/12/2015.
 */
public class RestError extends RestResponse{

    @SerializedName("code")
    public int code;

    @SerializedName("comment")
    public String message;

    public RestError(String message, int code)
    {
        this.message = message;
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "RestError{" +
                "code=" + code +
                ", comment='" + message + '\'' +
                '}';
    }
}