package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
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
import com.timappweb.timapp.entities.User;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

public class EditProfileActivity extends BaseActivity{

    String TAG = "EditProfileActivity";
    private Activity activity = this;
    private InputMethodManager imm;

    private HorizontalTagsRecyclerView horizontalTagsRecyclerView;
    private HorizontalTagsAdapter horizontalTagsAdapter;
    private EditText editText;
    private View skipView;
    private View submitView;
    private Button buttonSubmit;

    private int counterTags;

    private User currentUser;

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
        currentUser = IntentsUtils.extractUser(getIntent());

        init();

        initAdapter();
        setListener();
    }

    private void init() {
        counterTags = 0;
        editText.requestFocus();
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        //for hide/close keyboard
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void initAdapter() {
        horizontalTagsAdapter = horizontalTagsRecyclerView.getAdapter();
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
            //OnEditorAction returns false if we close the keyboard
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_NEXT) {
                    String value = editText.getText().toString();

                    boolean isTagValid = horizontalTagsAdapter.tryAddData(value);
                    horizontalTagsAdapter.notifyDataSetChanged();

                    if(isTagValid) {
                        setViewsAndCounter();
                    }
                    return true;
                }
                return false;
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser.tags = horizontalTagsAdapter.getData();
                //TODO Steph : Save tags
                IntentsUtils.profile(activity);
            }
        });

        horizontalTagsAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                removeTag(position);
                counterTags = counterTags - 2;
                setViewsAndCounter();
                editText.setVisibility(View.VISIBLE);
                skipView.setVisibility(View.VISIBLE);
                submitView.setVisibility(View.GONE);
                editText.requestFocus();
                imm.showSoftInput(editText, 0);
            }
        });
    }

    private void setViewsAndCounter() {
        counterTags = counterTags + 1;
        switch (counterTags) {
            case 0:
                editText.setHint(getResources().getString(R.string.hint_et_edit_first_profile));
                editText.setText("");
                break;
            case 1:
                editText.setHint(getResources().getString(R.string.hint_et_edit_first_profile2));
                editText.setText("");
                break;
            case 2:
                editText.setHint(getResources().getString(R.string.hint_et_edit_first_profile3));
                editText.setText("");
                break;
            case 3:
                editText.setVisibility(View.GONE);
                skipView.setVisibility(View.GONE);
                submitView.setVisibility(View.VISIBLE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);   //Hide keyboard
                break;
        }
    }

    private void removeTag(int position) {
        horizontalTagsAdapter.removeData(position);
        horizontalTagsAdapter.notifyDataSetChanged();
    }
}
