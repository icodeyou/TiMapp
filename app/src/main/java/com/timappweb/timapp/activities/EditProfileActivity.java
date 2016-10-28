package com.timappweb.timapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.listeners.OnBasicQueryTagListener;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;
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

    private SearchView searchView;
    private View submitView;
    private Button buttonSubmit;
    private View progressView;
    private SearchAndSelectTagManager searchAndSelectTagManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Toolbar
        int colorRes = ContextCompat.getColor(this, R.color.white);
        initToolbar(false, colorRes);

        searchView = (SearchView) findViewById(R.id.edit_text);
        buttonSubmit = (Button) findViewById(R.id.button_submit);
        submitView = findViewById(R.id.submit_view);
        progressView = findViewById(R.id.progress_view);

        initSearchView();
        setListener();
    }

    private void initSearchView() {
        searchView.setIconifiedByDefault(false);
        ImageView searchViewIcon =
                (ImageView)searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        ViewGroup linearLayoutSearchView = (ViewGroup) searchViewIcon.getParent();
        linearLayoutSearchView.removeView(searchViewIcon); //Remove magnifier Icon
        searchViewIcon.setVisibility(View.GONE);
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        searchView.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setHintTextColor(ContextCompat.getColor(this, R.color.color_hint_secondary));
        searchView.requestFocus();
        searchAndSelectTagManager = new SearchAndSelectTagManager(this,
                searchView,
                null,
                (HorizontalTagsRecyclerView) findViewById(R.id.selected_tags_profile),
                new OnBasicQueryTagListener(),
                buttonSubmit,
                submitView,
                null,
                3); //TODO [important] Get config from server instead of hard typing 3. (ConfigurationProvider.rules().max_tags)
    }


    private void setListener() {

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public synchronized void onClick(View v) {
                progressView.setVisibility(View.VISIBLE);
                Log.v(TAG, "Submitting user tags");
                final User user = MyApplication.getCurrentUser();
                RestClient
                        .put(ResourceUrlMapping.MODEL_USER, EditProfileMapper.toJson(searchAndSelectTagManager.getSelectedTags()))
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
    }


    private void finishActivityResult(){
        Intent intent = NavUtils.getParentActivityIntent(this);
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_KEY_TAG_LIST, (Serializable) searchAndSelectTagManager.getSelectedTags());
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
