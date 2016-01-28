package com.timappweb.timapp.rest;

import android.content.Context;

import com.timappweb.timapp.rest.model.RestFeedback;

import retrofit2.Response;

/**
 * Created by stephane on 1/28/2016.
 */
public abstract class RestFeedbackCallback extends RestCallback<RestFeedback> {

    public RestFeedbackCallback(Context context) {
        super(context);
    }

    public abstract void onActionSuccess(RestFeedback feedback);
    public abstract void onActionFail(RestFeedback feedback);

    @Override
    public void onResponse(Response<RestFeedback> response) {
        super.onResponse(response);
        if (response.isSuccess()){
            this.onActionSuccess(response.body());
        }
        else{
            this.onActionFail(response.body());
        }
    }

    @Override
    public void onFailure(Throwable t) {
        super.onFailure(t);
    }
}
