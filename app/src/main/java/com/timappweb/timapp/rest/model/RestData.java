package com.timappweb.timapp.rest.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by stephane on 9/12/2015.
 */
public class RestData<T> extends RestFeedback {

    @SerializedName("data")
    public T data;

    public T getData() {
        return data;
    }
}
