package com.timappweb.timapp.rest;

import android.content.Context;
import android.util.Log;

import com.timappweb.timapp.rest.model.RestFeedback;

import retrofit2.Response;

/**
 * Created by stephane on 1/28/2016.
 */
public abstract class RestFeedbackCallback extends RestCallback<RestFeedback> {

    private static final String TAG = "RestFeedbackCallback";

    public RestFeedbackCallback(Context context) {
        super(context);
    }

    public abstract void onActionSuccess(RestFeedback feedback);
    public abstract void onActionFail(RestFeedback feedback);

    @Override
    public void onResponse(Response<RestFeedback> response) {
        super.onResponse(response);
        if (response.isSuccess()){
            RestFeedback feedback = response.body();
            if (feedback.success){
                this.onActionSuccess(feedback);
            }
            else{
                this.onActionFail(feedback);
            }
        }
        else {
            this.onResponseFail(response);
        }
        this.onFinish();
    }

    private void onResponseFail(Response<RestFeedback> response) {
        Log.i(TAG, "Response fail: " + response.code());
        this.onFinish();
    }

    @Override
    public void onFailure(Throwable t) {
        super.onFailure(t);
    }

    public void onFinish(){}
}
