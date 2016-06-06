package com.timappweb.timapp.rest.callbacks;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.listeners.LoadingListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RestCallback<T> implements Callback<T> {

    private static final String TAG = "RestError";
    protected static Context context;

    protected LoadingListener listener = null;

    public RestCallback() {

    }

    public void onResponse200(Response<T> response){}

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (!response.isSuccessful()){
            String userMessage =  "";
            switch (response.code()){
                case 500:
                case 400:
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
    public void onFailure(Call<T> call, Throwable t) {
        Log.e(TAG, "::onFailure() -> " + t.getMessage());
        t.printStackTrace();
        this.onFinish();
    }

    public void onFinish(){
        if (this.listener != null){
            listener.onLoadEnd();
        }
    }

    public static void init(Context context) {
        RestCallback.context = context;
    }
}