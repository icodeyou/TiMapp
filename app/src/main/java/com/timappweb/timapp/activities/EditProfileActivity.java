package com.timappweb.timapp.activities;

import android.os.Bundle;

import com.timappweb.timapp.R;

public class EditProfileActivity extends BaseActivity{

    String TAG = "EditProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Toolbar
        this.initToolbar(true);

    }
}
