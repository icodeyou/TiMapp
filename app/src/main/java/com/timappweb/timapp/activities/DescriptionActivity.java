package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.timappweb.timapp.R;


public class DescriptionActivity extends BaseActivity {
    private String TAG = "DescriptionActivity";
    private InputMethodManager imm;
    private DescriptionActivity context;
    private EditText commentEt;

    //----------------------------------------------------------------------------------------------
    //Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);
        context = this;

        //this.initToolbar(true);

        final Activity activity = this;

        commentEt = (EditText) findViewById(R.id.comment_edit_text);
        View saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = commentEt.getText().toString();
                Log.d(TAG, "Saving description: " + description);
                Intent intent = new Intent(activity, AddPlaceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("description", description);
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
}
