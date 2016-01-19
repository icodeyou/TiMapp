package com.timappweb.timapp.rest;

import android.content.Context;
import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.rest.model.RestError;

import retrofit.Callback;
import retrofit.RetrofitError;

public abstract class RestCallback<T> implements Callback<T> {

    private static final String TAG = "RestError";
    protected Context context = null;

    public RestCallback(Context context){
        this.context = context;
    }

    public  void failure(RestError error){
        String userMessage = null;
        switch (error.getCode()){
            case 500:
            case 400:
                userMessage = context.getString(R.string.error_server_unavailable);
                break;
            case 300:
            case 403:
                userMessage = context.getString(R.string.error_require_login);//"You must be logged in to perform this action";
                break;
            case -1:
                userMessage = context.getString(R.string.error_no_internet);
                break;
            default:
                userMessage = context.getString(R.string.error_unkown);
        }
        MyApplication.showAlert(context, userMessage);
        Log.i(TAG, "Get server error: " + userMessage + "(" + error.toString() + ")");
    }


    @Override
    public void failure(RetrofitError error) {
        RestError restError = (RestError) error.getBodyAs(RestError.class);

        if (restError != null){
            failure(restError);
        }
        else {
            failure(new RestError(error.getMessage(), -1));
        }
    }


}