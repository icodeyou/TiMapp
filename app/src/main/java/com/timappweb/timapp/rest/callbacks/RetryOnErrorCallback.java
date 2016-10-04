package com.timappweb.timapp.rest.callbacks;

import android.content.Context;
import android.content.DialogInterface;

import com.timappweb.timapp.views.RetryDialog;

/**
 * Created by Stephane on 21/09/2016.
 */
public class RetryOnErrorCallback extends NetworkErrorCallback{

    private final OnRetryCallback callback;
    private boolean cancelable = true;

    public RetryOnErrorCallback(Context context, OnRetryCallback callback) {
        super(context);
        this.callback = callback;
    }

    public RetryOnErrorCallback setCancelable(boolean cancelable){
        this.cancelable = cancelable;
        return this;
    }

    @Override
    public void onError(Throwable error) {
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
