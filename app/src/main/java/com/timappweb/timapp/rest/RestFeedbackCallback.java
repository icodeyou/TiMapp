package com.timappweb.timapp.rest;

import android.content.Context;
import android.util.Log;

import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.rest.model.RestValidationErrors;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
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
    public void onResponse(Call<RestFeedback> call, Response<RestFeedback> response) {
        super.onResponse(call, response);
        if (response.isSuccessful()){
            RestFeedback feedback = response.body();
            this.onActionSuccess(feedback);
        }
        else if (response.code() == 400){
            if (response.errorBody() != null) {
                Converter<ResponseBody, RestFeedback> errorConverter =
                        RestClient.instance().getRetrofit().responseBodyConverter(RestFeedback.class, new Annotation[0]);
                RestFeedback error = null;
                try {
                    error = errorConverter.convert(response.errorBody());
                } catch (IOException e) {
                    Log.e(TAG, "Cannot convert error body from rest response");
                    e.printStackTrace();
                }
                this.onActionFail(error);
            }
            else{
                this.onActionFail(null);
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

    public void onFinish(){}
}
