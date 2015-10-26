package com.timappweb.timapp.rest;

import android.app.Activity;
import android.util.Log;

import com.timappweb.timapp.rest.model.RestError;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by stephane on 8/29/2015.
 */
public abstract class RestCallback<T> implements Callback<T> {

    private static final String TAG = "RestError";

    public  void failure(RestError error){

        switch (error.getCode()){
            case 500:
                // Server errore
                break;
            case 400:
                // Not found
                break;
            case 300:
                // Not authorized
                break;
            default:
                // TODO
        }
        Log.i(TAG, "Get server error: " + error.toString());
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