package com.timappweb.timapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.timappweb.timapp.R;

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
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.share_message_text));
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_message_subject));
                startActivity(Intent.createChooser(sharingIntent, "Share using"));
            }
        });

        shareButton.callOnClick();
    }
}
