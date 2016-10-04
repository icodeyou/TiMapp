package com.timappweb.timapp.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.timappweb.timapp.R;

/**
 * Created by Stephane on 06/09/2016.
 */
public class RetryDialog {

    private RetryDialog() {
    }

    public static AlertDialog.Builder builder(Context context, DialogInterface.OnClickListener listener){
        return RetryDialog.builder(context, listener,
                context.getResources().getString(R.string.no_network_access),
                context.getResources().getString(R.string.no_network_access))
            ;
    }


    public static AlertDialog.Builder builder(Context context, DialogInterface.OnClickListener listener, String title, String msg){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(context.getString(R.string.retry_button), listener);
        return builder;
    }


}
