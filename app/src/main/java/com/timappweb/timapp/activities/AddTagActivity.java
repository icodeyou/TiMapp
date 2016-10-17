package com.timappweb.timapp.activities;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.activeandroid.query.From;
import com.google.gson.JsonObject;
import com.greenfrvr.hashtagview.HashtagView;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.DataTransformTag;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventPost;
import com.timappweb.timapp.data.models.EventTag;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.listeners.OnAddTagListener;
import com.timappweb.timapp.listeners.OnBasicQueryTagListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;
import com.timappweb.timapp.managers.SearchTagDataProvider;
import com.timappweb.timapp.rest.ResourceUrlMapping;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.AutoMergeCallback;
import com.timappweb.timapp.rest.callbacks.FormErrorsCallback;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.NetworkErrorCallback;
import com.timappweb.timapp.rest.callbacks.PublishInEventCallback;
import com.timappweb.timapp.rest.io.serializers.AddEventPostMapper;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Response;

//TODO : searchAndSelectTagManager is used to add tags, but not to remove tags !

public class AddTagActivity extends BaseActivity{

    private String TAG = "AddTagActivity";
    private AddTagActivity context;

    // ---------------------------------------------------------------------------------------------
    //Views
    private HorizontalTagsRecyclerView              selectedTagsRV;
    private View                                    progressStartView;
    private Event                                   currentEvent = null;

    // @Bind(R.remote_id.hashtags1)
    protected HashtagView                           suggestedTagsView;

    //others
    private SearchAndSelectTagManager               searchAndSelectTagManager;
    private View                                    selectedTagsView;
    private EventPost                               eventEventPost;

    private Menu menu;
    private InputMethodManager imm;

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!IntentsUtils.requireLogin(this, false)
                || !LocationManager.hasLastLocation()){
            IntentsUtils.getBackToParent(this);
            return;
        }

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        this.currentEvent = IntentsUtils.extractEvent(getIntent());
        this.eventEventPost = new EventPost();
        this.eventEventPost.setLocation(LocationManager.getLastLocation());

        if (this.currentEvent == null){
            Log.d(TAG, "Event is null");
            IntentsUtils.getBackToParent(this);
            return;
        }

        setContentView(R.layout.activity_add_tag);
        this.initToolbar(false);
        selectedTagsView = findViewById(R.id.rv_selected_tags);
        selectedTagsRV = (HorizontalTagsRecyclerView) selectedTagsView;
        suggestedTagsView = (HashtagView) findViewById(R.id.rv_search_suggested_tags);
        progressStartView = findViewById(R.id.progress_view);
        //progressStartView.setVisibility(View.VISIBLE);

        initClickSelectedTag();
    }

    private void setInitialTags() {
        EventTag eventTag = IntentsUtils.extractEventTag(getIntent());
        if(eventTag != null) {
            searchAndSelectTagManager.addTag(eventTag.tag.getName());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_tags, menu);

        this.menu = menu;
        this.context = this;

        setSearchview(menu);
        searchView.requestFocus();
        searchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        initListTags();

        //set hint for searchview
        final OnBasicQueryTagListener onBasicQueryTagListener = new OnAddTagListener(this);
        searchAndSelectTagManager = new SearchAndSelectTagManager(this,
                searchView,
                suggestedTagsView,
                selectedTagsRV,
                onBasicQueryTagListener
        )
                .setDataProvider(new SearchTagDataProvider() {

                    @Override
                    public void onLoadEnds() {
                       //TODO Steph : This method is not called after tags loading (because tags  > 30 ?)
                        progressStartView.setVisibility(View.GONE);
                        suggestedTagsView.removeItem(searchAndSelectTagManager.getSelectedTags());
                        /*Animation animationTagsIn = AnimationUtils.loadAnimation(context, R.anim.appear_grow);
                        suggestedTagsView.setAnimation(animationTagsIn);*/
                    }

                    @Override
                    public void load(String term) {
                        //progressStartView.setVisibility(View.VISIBLE);
                        int tagLimit = ConfigurationProvider.rules().tags_suggest_limit;
                        // Get local tags (suggest in first tag already added to this event, then tag that are in db order by popularity)
                        From query = Tag.querySuggestTagForEvent(currentEvent)
                                .limit(tagLimit);
                        if (term != null && term.length() > 0){
                            query.where("Tag.name LIKE ? OR Tag.name = ?", term, term); // TODO add %%
                        }
                        List<Tag> tags = query.execute();
                        Log.d(TAG, "Tag in local db: " + tags.size() + "/" + tagLimit);
                        // If not enough tags locally, we get more on the server
                        if (tags.size() < tagLimit){
                            super.load(term);
                        }
                        else if (tags.size() > 0){
                            manager.getSearchHistory().onSearchResponse(term, tags, true);
                        }
                    }
                });
        searchAndSelectTagManager.loadTags("");

        //Initialize Query hint in searchview
        setInitialTags();
        actionCounter();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_post:
                postTags();
                return true;
            case R.id.home:
                finish();
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
                if (!searchAndSelectTagManager.hasSelectedTag(tag)){
                    if (searchAndSelectTagManager.addTag(tag.getName())){
                        suggestedTagsView.removeItem(item);
                        searchView.setIconified(false);
                        actionCounter();
                    }
                }
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

    public boolean actionCounter() {
        // If we ever want to update the keyboard by calling setImeOptions,
        // we need to clear the focus of the searchview.
        int numberTags = searchAndSelectTagManager.getSelectedTags().size();
        menu.findItem(R.id.action_post).setEnabled(numberTags>=selectedTagsRV.getMinTags());

        if(numberTags == selectedTagsRV.getMaxTags()-1) {
            searchView.setVisibility(View.VISIBLE);
            return true;
        } else if(numberTags == selectedTagsRV.getMaxTags()) {
            searchView.setVisibility(View.GONE);
            return true;
        } else {
            switch (numberTags) {
                case 0:
                    setSelectedTagsViewGone();
                    searchView.setQueryHint(getResources().getString(R.string.searchview_hint_no_tags));
                    return true;
                default:
                    // TODO Jack change this... Use formated string .. See doc
                    setSelectedTagsViewVisible();
                    String string1 = getResources().getString(R.string.searchview_hint_few_tags_part_one);
                    String string2 = getResources().getString(R.string.searchview_hint_few_tags_part_two);
                    searchView.setQueryHint(string1 + selectedTagsRV.getMaxTags() + string2);
                    return false;
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    //Public methods
    private void setSelectedTagsViewGone() {
        selectedTagsView.setVisibility(View.GONE);
        findViewById(R.id.bottom_line_hrv).setVisibility(View.GONE);
    }

    private void setSelectedTagsViewVisible() {
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
        menu.findItem(R.id.action_post).setEnabled(false);
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
                .onResponse(new PublishInEventCallback(currentEvent, MyApplication.getCurrentUser(), QuotaType.ADD_TAGS))
                .onResponse(new FormErrorsCallback(AddTagActivity.this, "Posts"))
                .onResponse(new HttpCallback<JsonObject>() {
                    @Override
                    public void successful(JsonObject feedback) {
                        Log.i(TAG, "EventPost has been saved with id: " + eventEventPost.remote_id);
                        eventEventPost.mySaveSafeCall();
                        EventTag.incrementCountRef(currentEvent, eventEventPost.getTags());
                        Toast.makeText(AddTagActivity.this, R.string.thanks_for_add_tag, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void notSuccessful() {
                        Toast.makeText(AddTagActivity.this, R.string.form_invalid_input, Toast.LENGTH_LONG).show();
                    }
                })
                .onError(new NetworkErrorCallback(AddTagActivity.this))
                .onFinally(new HttpCallManager.FinallyCallback(){
                    @Override
                    public void onFinally(Response response, Throwable error) {
                        menu.findItem(R.id.action_post).setEnabled(true);
                    }
                })
                .perform();
    }
}
