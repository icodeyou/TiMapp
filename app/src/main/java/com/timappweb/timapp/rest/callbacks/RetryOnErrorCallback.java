package com.timappweb.timapp.rest.callbacks;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.views.RetryDialog;

import java.io.IOException;

/**
 * Created by Stephane on 21/09/2016.
 */
public class RetryOnErrorCallback extends NetworkErrorCallback{

    private static final String TAG = "RetryOnErrorCallback";
    private final OnRetryCallback callback;
    private boolean cancelable = true;

    public RetryOnErrorCallback(Activity activity, OnRetryCallback callback) {
        super(activity);
        this.callback = callback;
    }

    public RetryOnErrorCallback(Activity activity, final HttpCallManager remoteCall) {
        this(activity, new OnRetryCallback() {
            @Override
            public void onRetry() {
                remoteCall.retry();
            }
        });
    }

    public RetryOnErrorCallback setCancelable(boolean cancelable){
        this.cancelable = cancelable;
        return this;
    }

    @Override
    public void network(IOException error) {
        if (context instanceof Activity){
            if (((Activity) context).isFinishing()){
                Log.w(TAG, "Activity is finishing. No retry required");
                return ;
            }
        }
        else{
            return;
        }
        RetryDialog.builder(context, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                callback.onRetry();
            }
        })
        .setCancelable(cancelable)
        .create()
        .show();
    }

    public interface OnRetryCallback{
        void onRetry();
    }

}
