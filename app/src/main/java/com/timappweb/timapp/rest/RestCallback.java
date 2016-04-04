package com.timappweb.timapp.rest;

import android.content.Context;
import android.util.Log;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;

import retrofit2.Callback;
import retrofit2.Response;


public class RestCallback<T> implements Callback<T> {
    private static final String TAG = "RestError";
    protected Context context = null;



    public RestCallback(Context context){
        this.context = context;
    }

    public void onResponse200(Response<T> response){

    }



    @Override
    public void onResponse(Response<T> response) {
        if (!response.isSuccess()){
            String userMessage =  "";
            switch (response.code()){
                case 500:
                case 400:
                    userMessage = context.getString(R.string.error_server_unavailable);
                    break;
                case 300:
                case 403:
                    IntentsUtils.login(this.context);
                    return;
                case -1:
                    userMessage = context.getString(R.string.error_no_internet);
                    break;
                default:
                    userMessage = context.getString(R.string.error_unkown);
            }
            Log.i(TAG, "Get server error: " + userMessage + "(" + response.errorBody() + ")");
        }
        else{
            this.onResponse200(response);
        }
        this.onFinish();
    }

    @Override
    public void onFailure(Throwable t) {
        Log.e(TAG, "::onFailure() -> " + t.getMessage());
        this.onFinish();
    }

    public void onFinish(){}

}