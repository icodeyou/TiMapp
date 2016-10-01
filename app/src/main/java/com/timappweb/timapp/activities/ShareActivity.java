package com.timappweb.timapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;

public class ShareActivity extends BaseActivity {
    private ImageView shareButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        this.initToolbar(true);

        shareButton = (ImageView) findViewById(R.id.share_button);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.actionShareApp(ShareActivity.this);
            }
        });

        shareButton.callOnClick();
    }
}
