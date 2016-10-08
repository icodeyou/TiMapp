package com.timappweb.timapp.rest.callbacks;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.views.RetryDialog;

/**
 * Created by Stephane on 21/09/2016.
 */
public class RetryOnErrorCallback extends NetworkErrorCallback{

    private static final String TAG = "RetryOnErrorCallback";
    private final OnRetryCallback callback;
    private boolean cancelable = true;

    public RetryOnErrorCallback(Context context, OnRetryCallback callback) {
        super(context);
        this.callback = callback;
    }

    public RetryOnErrorCallback(Context context, final HttpCallManager remoteCall) {
        this(context, new OnRetryCallback() {
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
    public void onError(Throwable error) {
        if (context instanceof Activity){
            if (((Activity) context).isFinishing()){
                Log.w(TAG, "Activity is finishing. Not retry required");
                return ;
            }
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
