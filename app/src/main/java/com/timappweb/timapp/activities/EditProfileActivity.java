package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
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

import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserTag;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.ResourceUrlMapping;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.AutoMergeCallback;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.NetworkErrorCallback;
import com.timappweb.timapp.rest.io.serializers.EditProfileMapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.io.Serializable;

import retrofit2.Response;

public class EditProfileActivity extends BaseActivity{

    public static final String EXTRA_KEY_TAG_LIST  = "tag_list";
    private static final String TAG                 = "EditProfileActivity";

    private InputMethodManager imm;

    private HorizontalTagsRecyclerView horizontalTagsRecyclerView;
    private HorizontalTagsAdapter horizontalTagsAdapter;
    private EditText editText;
    private TextView counterView;
    private View submitView;
    private Button buttonSubmit;
    private View progressView;

    private int counterTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Toolbar
        int colorRes = ContextCompat.getColor(this, R.color.white);
        initToolbar(false, colorRes);

        horizontalTagsRecyclerView = (HorizontalTagsRecyclerView) findViewById(R.id.selected_tags_profile);
        editText = (EditText) findViewById(R.id.edit_text);
        counterView = (TextView) findViewById(R.id.counter_view);
        buttonSubmit = (Button) findViewById(R.id.button_submit);
        submitView = findViewById(R.id.submit_view);
        progressView = findViewById(R.id.progress_view);

        init();

        horizontalTagsAdapter = horizontalTagsRecyclerView.getAdapter();
        setListener();
    }

    private void init() {
        counterTags = 0;
        editText.requestFocus();
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        editText.getBackground().mutate().setColorFilter(getResources().getColor(R.color.White), PorterDuff.Mode.SRC_ATOP);

        //for hide/close keyboard
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    String value = editText.getText().toString();

                    boolean isTagValid = horizontalTagsAdapter.tryAddData(value);
                    horizontalTagsAdapter.notifyDataSetChanged();

                    if (isTagValid) {
                        setViewsAndCounter();
                    }
                    return true;
                }
                return false;
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public synchronized void onClick(View v) {
                progressView.setVisibility(View.VISIBLE);
                Log.v(TAG, "Submitting user tags");
                final User user = MyApplication.getCurrentUser();
                RestClient
                        .put(ResourceUrlMapping.MODEL_USER, EditProfileMapper.toJson(horizontalTagsAdapter.getData()))
                        .onResponse(new AutoMergeCallback(user))
                        .onResponse(new HttpCallback<JsonObject>() {
                            @Override
                            public void successful(JsonObject feedback) throws CannotSaveModelException {
                                user.deepSave();
                                Toast.makeText(EditProfileActivity.this, R.string.toast_profile_saved, Toast.LENGTH_LONG).show();
                                finishActivityResult();
                            }

                            @Override
                            public void notSuccessful() {
                                Toast.makeText(EditProfileActivity.this, R.string.cannot_save_your_profile, Toast.LENGTH_LONG).show();
                            }
                        })
                        .onError(new NetworkErrorCallback(EditProfileActivity.this))
                        .onFinally(new HttpCallManager.FinallyCallback() {
                            @Override
                            public void onFinally(Response response, Throwable error) {
                                progressView.setVisibility(View.GONE);
                                if (error != null){
                                    Toast.makeText(EditProfileActivity.this, R.string.cannot_save_your_profile, Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .perform();
            }
        });

        horizontalTagsAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                removeTag(position);
                counterTags = counterTags - 2;
                setViewsAndCounter();
                editText.setVisibility(View.VISIBLE);
                counterView.setVisibility(View.VISIBLE);
                submitView.setVisibility(View.INVISIBLE);
                editText.requestFocus();
                imm.showSoftInput(editText, 0);
            }
        });
    }


    private void finishActivityResult(){
        Intent intent = NavUtils.getParentActivityIntent(this);
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_KEY_TAG_LIST, (Serializable) horizontalTagsAdapter.getData());
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void setViewsAndCounter() {
        counterTags = counterTags + 1;
        switch (counterTags) {
            case 0:
                counterView.setText(getResources().getString(R.string.text_tags_left_3));
                editText.setText("");
                break;
            case 1:
                counterView.setText(getResources().getString(R.string.text_tags_left_2));
                editText.setText("");
                break;
            case 2:
                counterView.setText(getResources().getString(R.string.text_tags_left_1));
                editText.setText("");
                break;
            case 3:
                editText.setVisibility(View.GONE);
                counterView.setVisibility(View.GONE);
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
