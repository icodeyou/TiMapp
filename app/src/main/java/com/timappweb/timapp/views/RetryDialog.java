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

    public static void show(Context context, DialogInterface.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.no_network_access));
        builder.setMessage(context.getResources().getString(R.string.no_network_access));
        builder.setPositiveButton("Retry", listener);
        builder.create().show();
    }


}
