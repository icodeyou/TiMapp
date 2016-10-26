package com.timappweb.timapp.activities;

import android.os.Bundle;

import com.timappweb.timapp.views.UpdateAppDialog;

public class AppUpdateActivity extends BaseActivity  {

    private static final String TAG = "AppUpdateActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UpdateAppDialog.builder(this).create().show();
    }

}
