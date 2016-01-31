package com.timappweb.timapp.activities;

import android.app.Activity;
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

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.List;

public class EditProfileActivity extends BaseActivity{

    String TAG = "EditProfileActivity";
    private Activity activity = this;

    private HorizontalTagsRecyclerView horizontalTagsRecyclerView;
    private HorizontalTagsAdapter horizontalTagsAdapter;
    private EditText editText;
    private View skipView;
    private View submitView;
    private Button buttonSubmit;

    private int counterTags;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        this.initToolbar(false);

        horizontalTagsRecyclerView = (HorizontalTagsRecyclerView) findViewById(R.id.selected_tags_profile);
        editText = (EditText) findViewById(R.id.edit_text);
        skipView = findViewById(R.id.skip_button);
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

        //for keyboard
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
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
                    Toast.makeText(EditProfileActivity.this, R.string.toast_no_space, Toast.LENGTH_SHORT).show();
                    string = string.substring(0, string.length()-1);
                    editText.setText(string);
                    editText.setSelection(string.length());
                }
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (!editText.getText().toString().isEmpty() && actionId == EditorInfo.IME_ACTION_DONE) {
                    if(horizontalTagsAdapter.isOneSimilarValue(editText.getText().toString())) {
                        Toast.makeText(EditProfileActivity.this, R.string.toast_tag_already_chosen, Toast.LENGTH_SHORT).show();
                        editText.setText("");
                        //Doesnt work :
                        // imm.showSoftInput(editText,0);
                        // editText.requestfocus();
//                        editText.performClick();
//                        editText.callOnClick();
                        return false;
                    }
                    addTag();
                    setViewsAndCounter();
                    return true;
                }
                return false;
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.getCurrentUser().tags = horizontalTagsAdapter.getData();
                IntentsUtils.profile(activity);
            }
        });

        horizontalTagsAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                removeTag(position);
                counterTags = counterTags - 2;
                setViewsAndCounter();
                editText.requestFocus();
            }
        });
    }

    private void setViewsAndCounter() {
        switch (counterTags) {
            case -1:
                editText.setHint(getResources().getString(R.string.hint_et_edit_first_profile));
                editText.setText("");
                break;
            case 0:
                editText.setHint(getResources().getString(R.string.hint_et_edit_first_profile2));
                editText.setText("");
                break;
            case 1:
                editText.setHint(getResources().getString(R.string.hint_et_edit_first_profile3));
                editText.setText("");
                editText.setVisibility(View.VISIBLE);
                skipView.setVisibility(View.VISIBLE);
                submitView.setVisibility(View.GONE);
                break;
            case 2:
                //Hide keyboard
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                editText.setVisibility(View.GONE);
                skipView.setVisibility(View.GONE);
                submitView.setVisibility(View.VISIBLE);
                break;
        }

        counterTags= counterTags + 1;

    }

    private void addTag() {
        horizontalTagsAdapter.addData(editText.getText().toString());
        horizontalTagsAdapter.notifyDataSetChanged();
    }

    private void removeTag(int position) {
        horizontalTagsAdapter.removeData(position);
        horizontalTagsAdapter.notifyDataSetChanged();
    }
}
