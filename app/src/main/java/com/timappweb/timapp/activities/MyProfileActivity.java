package com.timappweb.timapp.activities;

import android.os.Bundle;

import com.timappweb.timapp.R;

public class MyProfileActivity extends BaseActivity{

    String TAG = "MyProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Toolbar
        this.initToolbar(true);

    }
}
