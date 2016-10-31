package com.timappweb.timapp.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.IntentsUtils;

public class AppUpdateActivity extends BaseActivity  {

    private static final String TAG = "AppUpdateActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Log.w(TAG, "App version = " + ". Requ)

        builder.setTitle(this.getString(R.string.app_update_required));
        builder.setPositiveButton(this.getString(R.string.alert_dialog_update_app), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                IntentsUtils.updateAppPlayestore(AppUpdateActivity.this);
                dialog.dismiss();
                AppUpdateActivity.this.finish();
            }
        });
        builder.setNegativeButton(this.getString(R.string.quit_timapp), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                AppUpdateActivity.this.finish();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

}
