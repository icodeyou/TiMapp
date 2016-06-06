package com.timappweb.timapp.rest;

import com.timappweb.timapp.listeners.LoadingListener;
import com.timappweb.timapp.rest.callbacks.RestCallback;

import retrofit2.Call;

/**
 * Created by stephane on 4/6/2016.
 */
public class ApiCallFactory {

    public static Call build(Call call, RestCallback callback, LoadingListener listener){
        listener.onLoadStart();
        call.enqueue(callback);
        return call;
    }
    public static Call build(Call call, RestCallback callback){
        call.enqueue(callback);
        return call;
    }
}
