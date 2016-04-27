package com.timappweb.timapp.activities;

import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import com.timappweb.timapp.R;


public class CommentActivity extends BaseActivity {
    private String TAG = "CommentActivity";
    private InputMethodManager imm;
    private CommentActivity context;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        context = this;

        this.initToolbar(true);
    }

}
