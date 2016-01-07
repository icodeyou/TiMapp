package com.timappweb.timapp.activities;

import android.os.Bundle;

import com.timappweb.timapp.R;

public class AddSpotActivity extends BaseActivity {
    private String TAG = "PublishActivity";

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);
        this.initToolbar(true);
    }

    //----------------------------------------------------------------------------------------------
    //Private methods

    //----------------------------------------------------------------------------------------------
    //Inner classes

    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    //----------------------------------------------------------------------------------------------
    //Miscellaneous
}
