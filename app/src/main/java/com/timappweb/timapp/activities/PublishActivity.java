package com.timappweb.timapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.MyLocationProvider;

public class PublishActivity extends BaseActivity{

    private MyLocationProvider locationProvider;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);
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
    public void testClick(View view) {
        Intent intent = new Intent(this,PublishActivity.class);
        startActivity(intent);
    }
}
