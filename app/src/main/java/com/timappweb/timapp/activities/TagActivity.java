package com.timappweb.timapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.timappweb.timapp.R;

public class TagActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        this.initToolbar(true);
    }

    public void testClick(View view) {
        Intent intent = new Intent(this,PublishActivity.class);
        startActivity(intent);
    }
}
