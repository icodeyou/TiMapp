package com.timappweb.timapp.rest.callbacks;

import android.content.Context;
import android.content.DialogInterface;

import com.timappweb.timapp.views.RetryDialog;

/**
 * Created by Stephane on 21/09/2016.
 */
public class RetryOnErrorCallback extends NetworkErrorCallback{

    private final OnRetryCallback callback;

    public RetryOnErrorCallback(Context context, OnRetryCallback callback) {
        super(context);
        this.callback = callback;
    }

    @Override
    public void onError(Throwable error) {
        RetryDialog.show(context, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                callback.onRetry();
            }
        });

    }

    public interface OnRetryCallback{
        void onRetry();
    }

}
