package com.timappweb.timapp.activities;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.greenfrvr.hashtagview.HashtagView;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.DataTransformTag;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventTag;
import com.timappweb.timapp.data.models.EventPost;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.listeners.OnBasicQueryTagListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.listeners.OnAddTagListener;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;
import com.timappweb.timapp.managers.SearchTagDataProvider;
import com.timappweb.timapp.rest.ResourceUrlMapping;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.AutoMergeCallback;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.PublishInEventCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.rest.io.serializers.AddEventPostMapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Response;

public class AddTagActivity extends BaseActivity{

    private String TAG = "AddTagActivity";

    // ---------------------------------------------------------------------------------------------
    //Views
    private HorizontalTagsRecyclerView              selectedTagsRV;
    private View progressStartView;
    private Event                                   currentEvent = null;

    // @Bind(R.remote_id.hashtags1)
    protected HashtagView                           suggestedTagsView;

    //others
    private SearchAndSelectTagManager               searchAndSelectTagManager;
    private View                                    selectedTagsView;
    private EventPost                               eventEventPost;

    private Menu menu;

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!IntentsUtils.requireLogin(this, false)){
            finish();
            return;
        }

        this.currentEvent = IntentsUtils.extractEvent(getIntent());
        this.eventEventPost = new EventPost();
        this.eventEventPost.setLocation(LocationManager.getLastLocation());

        if (this.currentEvent == null){
            Log.d(TAG, "Event is null");
            IntentsUtils.home(this);
            return;
        }

        setContentView(R.layout.activity_add_tag);
        this.initToolbar(false);
        selectedTagsView = findViewById(R.id.rv_selected_tags);
        selectedTagsRV = (HorizontalTagsRecyclerView) selectedTagsView;
        suggestedTagsView = (HashtagView) findViewById(R.id.rv_search_suggested_tags);
        progressStartView = findViewById(R.id.progress_view);

        initClickSelectedTag();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_tags, menu);

        this.menu = menu;

        setSearchview(menu);
        searchView.requestFocus();
        searchView.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        searchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        initListTags();

        //set hint for searchview
        final OnBasicQueryTagListener onBasicQueryTagListener = new OnAddTagListener(this);
        searchAndSelectTagManager = new SearchAndSelectTagManager(this,
                searchView,
                suggestedTagsView,
                selectedTagsRV,
                onBasicQueryTagListener,
                new SearchTagDataProvider() {
                    @Override
                    public void onLoadEnds() {
                        progressStartView.setVisibility(View.GONE);
                    }
                }
        );


        //Initialize Query hint in searchview
        actionCounter();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_validate:
                String query = searchView.getQuery().toString();
                if(query.isEmpty()) {
                    Toast.makeText(this, R.string.select_at_least_one_tag, Toast.LENGTH_SHORT).show();
                }
                else {
                    searchView.setQuery(query, true);
                }
                return true;
            case R.id.action_post:
                postTags();
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IntentsUtils.REQUEST_PUBLISH && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }


    //----------------------------------------------------------------------------------------------
    //Private methods

    private void initListTags() {
        suggestedTagsView.addOnTagClickListener(new HashtagView.TagsClickListener() {
            @Override
            public void onItemClicked(Object item) {
                Tag tag = (Tag) item;

                boolean success = suggestedTagsView.removeItem(item);
                Log.d(TAG, "removing item success : "+ success);

                searchView.setQuery(tag.name, true);
                searchView.clearFocus();
            }
        });

        suggestedTagsView.setData(new LinkedList<Tag>(), new DataTransformTag());
    }

    private void initClickSelectedTag() {
        selectedTagsRV.getAdapter().setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                Log.d(TAG, "Clicked on selected item");
                Tag tag = selectedTagsRV.getAdapter().getData(position);
                suggestedTagsView.addItem(tag);
                selectedTagsRV.getAdapter().removeData(position);
                actionCounter();
            }
        });
    }

    public void actionCounter() {
        if(searchAndSelectTagManager.getSelectedTags().size()==0) {
            setSelectedTagsViewGone();
            searchView.setQueryHint(getResources().getString(R.string.searchview_hint_no_tags));
        }
        else {
            setSelectedTagsViewVisible();
            String string1 = getResources().getString(R.string.searchview_hint_few_tags_part_one);
            String string2 = getResources().getString(R.string.searchview_hint_few_tags_part_two);
            searchView.setQueryHint(string1 + selectedTagsRV.getMaxTags() + string2);
        }
        if(searchAndSelectTagManager.getSelectedTags().size()==selectedTagsRV.getMaxTags()) {
            searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
    }

    //----------------------------------------------------------------------------------------------
    //Public methods
    private void setSelectedTagsViewGone() {
        findViewById(R.id.top_line_hrv).setVisibility(View.GONE);
        selectedTagsView.setVisibility(View.GONE);
        findViewById(R.id.bottom_line_hrv).setVisibility(View.GONE);
    }

    private void setSelectedTagsViewVisible() {
        findViewById(R.id.top_line_hrv).setVisibility(View.VISIBLE);
        selectedTagsView.setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_line_hrv).setVisibility(View.VISIBLE);
    }

    public void simulateKeys() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Instrumentation inst = new Instrumentation();
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_SPACE);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
            }
        }).start();
    }

    private void postTags() {
        menu.findItem(R.id.action_post).setEnabled(true);

        eventEventPost.setTags(searchAndSelectTagManager.getSelectedTags());
        eventEventPost.event = currentEvent;
        // Validating user input
        if (!eventEventPost.validateForSubmit()) {
            Toast.makeText(AddTagActivity.this, R.string.form_invalid_input, Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "Submitting eventPost: " + eventEventPost);

        RestClient
                .post(ResourceUrlMapping.MODEL_EVENT_POST, AddEventPostMapper.toJson(eventEventPost))
                .onResponse(new AutoMergeCallback(eventEventPost))
                .onResponse(new PublishInEventCallback(currentEvent, MyApplication.getCurrentUser(), QuotaType.ADD_POST))
                .onResponse(new HttpCallback<JsonObject>() {
                    @Override
                    public void successful(JsonObject feedback) {
                        Log.i(TAG, "EventPost has been saved with id: " + eventEventPost.remote_id);
                        eventEventPost.deepSave();
                        EventTag.incrementCountRef(currentEvent, eventEventPost.getTags());
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void notSuccessful() {
                        Toast.makeText(AddTagActivity.this, R.string.form_invalid_input, Toast.LENGTH_LONG).show();
                    }
                })
                .onFinally(new HttpCallManager.FinallyCallback(){
                    @Override
                    public void onFinally(Response response, Throwable error) {
                        menu.findItem(R.id.action_post).setEnabled(false);
                    }
                })
                .perform();
    }
}
