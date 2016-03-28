package com.timappweb.timapp.activities;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by stephane on 3/26/2016.
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, DrawerActivity.class);
        startActivity(intent);
        finish();
    }

}
