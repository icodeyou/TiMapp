package com.timappweb.timapp.rest.io.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Created by stephane on 9/12/2015.
 */
public class ClientError{

    @SerializedName("code")
    @Expose
    public int code;

    @SerializedName("message")
    @Expose
    public String message;

    @SerializedName("app_code")
    @Expose
    public int appCode;

    public ClientError(){}

    public int getCode() {
        return code;
    }

    public int getAppCode() {
        return appCode;
    }

    @Override
    public String toString() {
        return "RestError{" +
                "code=" + code +
                ", app_code='" + appCode + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}