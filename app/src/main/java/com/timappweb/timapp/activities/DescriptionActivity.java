package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;


public class DescriptionActivity extends BaseActivity {
    private String TAG = "DescriptionActivity";
    private InputMethodManager imm;
    private EditText commentEt;
    private View saveButton;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        //this.initToolbar(true);

        commentEt = (EditText) findViewById(R.id.comment_edit_text);
        saveButton = findViewById(R.id.save_button);

        initEt();
        setListeners();
    }

    private void setListeners() {
        final Activity activity = this;
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = commentEt.getText().toString();
                Log.d(TAG, "Saving description: " + description);
                IntentsUtils.exitDescriptionActivity(activity, description);
                finish();
            }
        });
    }

    private void initEt() {
        commentEt.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        String comment = IntentsUtils.extractComment(getIntent());
        commentEt.setText(comment);
        commentEt.setSelection(commentEt.getText().length());
    }
}
