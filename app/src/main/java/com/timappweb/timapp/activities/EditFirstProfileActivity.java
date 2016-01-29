package com.timappweb.timapp.activities;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

public class EditFirstProfileActivity extends BaseActivity{

    String TAG = "EditProfileActivity";

    private HorizontalTagsRecyclerView horizontalTagsRecyclerView;
    private HorizontalTagsAdapter horizontalTagsAdapter;
    private EditText editText;
    private Button skipButton;
    private Button buttonSubmit;
    private View submitView;

    private int counterTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_first_profile);
        this.initToolbar(true);

        horizontalTagsRecyclerView = (HorizontalTagsRecyclerView) findViewById(R.id.selected_tags_profile);
        editText = (EditText) findViewById(R.id.edit_text);
        skipButton = (Button) findViewById(R.id.skip_button);
        buttonSubmit = (Button) findViewById(R.id.button_submit);
        submitView = findViewById(R.id.submit_view);

        init();

        initAdapter();
        setListener();

    }

    private void init() {
        counterTags = 0;
        editText.requestFocus();
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }

    private void initAdapter() {
        horizontalTagsAdapter = new HorizontalTagsAdapter(this);
        horizontalTagsRecyclerView.setAdapter(horizontalTagsAdapter);
    }


    private void setListener() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                if (string.contains(" ")) {
                    Toast.makeText(EditFirstProfileActivity.this, R.string.toast_no_space, Toast.LENGTH_SHORT).show();
                    string = string.substring(0, string.length()-1);
                    editText.setText(string);
                    editText.setSelection(2);
                }
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onSubmitTag();
                    return true;
                }
                return false;
            }
        });
    }

    private void onSubmitTag() {

        switch (counterTags) {
            case 0:
                horizontalTagsAdapter.addData(editText.getText().toString());
                horizontalTagsAdapter.notifyDataSetChanged();
                editText.setText("");
                break;
            case 1:
                horizontalTagsAdapter.addData(editText.getText().toString());
                horizontalTagsAdapter.notifyDataSetChanged();
                editText.setText("");
                break;
            case 2:
                horizontalTagsAdapter.addData(editText.getText().toString());
                horizontalTagsAdapter.notifyDataSetChanged();

                //Hide keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                editText.setVisibility(View.GONE);
                skipButton.setVisibility(View.GONE);
                submitView.setVisibility(View.VISIBLE);
                break;
        }
        counterTags= counterTags + 1;
    }

}
