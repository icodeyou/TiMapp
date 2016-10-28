package com.timappweb.timapp.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;

/**
 * Created by Stephane on 06/09/2016.
 */
public class UpdateAppDialog {

    private UpdateAppDialog() {}


    public static AlertDialog.Builder builder(final Activity context){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.app_update_required));
        builder.setPositiveButton(context.getString(R.string.alert_dialog_update_app), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                IntentsUtils.updateAppPlayestore(context);
                dialog.dismiss();
                context.finish();
            }
        });
        builder.setNegativeButton(context.getString(R.string.quit_timapp), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                context.finish();
            }
        });
        builder.setCancelable(true);
        return builder;
    }


}
