package com.timappweb.timapp.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.timappweb.timapp.R;

/**
 * Created by Stephane on 06/09/2016.
 */
public class ConfirmDialog {

    private ConfirmDialog() {
    }


    public static AlertDialog.Builder builder(
            Context context, String title, String mainMsg, String confirmMessage,
            DialogInterface.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(title != null) {
            builder.setTitle(title);
        }
        builder.setMessage(mainMsg);
        builder.setPositiveButton(confirmMessage, listener);
        builder.setNegativeButton(context.getString(R.string.alert_dialog_cancel), listener);
        return builder;
    }


}
