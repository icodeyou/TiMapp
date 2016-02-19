package com.timappweb.timapp.activities;


import android.os.Bundle;
import android.util.Log;

import com.timappweb.timapp.R;


public class IncomingActivity extends BaseActivity {
    private String TAG = "IncomingActivity";

    // ----------------------------------------------------------------------------------------------
    //OVERRIDE METHODS
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "Creating LocateActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming);
        this.initToolbar(false);
    }

}
