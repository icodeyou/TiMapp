package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;

import io.fabric.sdk.android.Fabric;

/**
 * Created by stephane on 3/29/2016.
 */
public class ErrorActivity extends BaseActivity {

    private static final String TAG = "ErrorActivity";
    private TextView tvErrorMessage;
    private TextView tvErrorTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        tvErrorMessage = (TextView) findViewById(R.id.tv_error_message);
        tvErrorTitle = (TextView) findViewById(R.id.tv_error_title);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(IntentsUtils.KEY_TITLE) && bundle.containsKey(IntentsUtils.KEY_MESSAGE)){
            String title = bundle.getString(IntentsUtils.KEY_TITLE);
            String message = bundle.getString(IntentsUtils.KEY_MESSAGE);
            tvErrorTitle.setText(title);
            tvErrorMessage.setText(message);
        }
        else{
            tvErrorTitle.setText(getResources().getString(R.string.fatal_error_unknown_reason_title));
            tvErrorMessage.setText(getResources().getString(R.string.fatal_error_unknown_reason_message));
        }
    }
}
