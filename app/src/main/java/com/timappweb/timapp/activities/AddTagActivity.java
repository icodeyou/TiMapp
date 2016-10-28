package com.timappweb.timapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.activeandroid.query.From;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventPost;
import com.timappweb.timapp.data.models.EventTag;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.listeners.OnSuggestQueryListener;
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

    private EventPost                               eventEventPost;

    private Menu menu;

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!IntentsUtils.requireLogin(this, false)
                || !LocationManager.hasLastLocation()){
            IntentsUtils.getBackToParent(this);
            return;
        }

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
        View selectedTagsView = findViewById(R.id.rv_selected_tags);
        selectedTagsRV = (HorizontalTagsRecyclerView) selectedTagsView;
        progressStartView = findViewById(R.id.progress_view);
        //progressStartView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_tags, menu);

        this.menu = menu;
        this.context = this;

        SearchView searchView = initSearchView(menu);
        searchView.requestFocus();
        searchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        //set hint for searchview;
        SearchAndSelectTagManager searchAndSelectTagManager = new SearchAndSelectTagManager(this,
                searchView,
                (RecyclerView) findViewById(R.id.rv_suggested_tags),
                selectedTagsRV,
                new OnSuggestQueryListener(),
                findViewById(R.id.action_validate_tags),
                findViewById(R.id.check_layout),
                findViewById(R.id.bottom_line_hrv),
                ConfigurationProvider.rules().posts_max_tags_number
        )
                .setDataProvider(new SearchTagDataProvider() {

                    @Override
                    public void onLoadEnds() {
                        //TODO Steph : This method is not called after tags loading (because tags  > 30 ?)
                        /*progressStartView.setVisibility(View.GONE);
                        suggestedTagsView.removeItem(searchAndSelectTagManager.getSelectedTags());
                        Animation animationTagsIn = AnimationUtils.loadAnimation(context, R.anim.scale_up);
                        suggestedTagsView.setAnimation(animationTagsIn);*/
                    }

                    @Override
                    public void load(String term) {
                        //progressStartView.setVisibility(View.VISIBLE);
                        int tagLimit = ConfigurationProvider.rules().tags_suggest_limit;
                        // Get local tags (suggest in first tag already added to this event, then tag that are in db order by popularity)
                        From query = Tag.querySuggestTagForEvent(currentEvent)
                                .limit(tagLimit);
                        if (term != null && term.length() > 0) {
                            query.where("Tag.name LIKE ? OR Tag.name = ?", term, term); // TODO add wildchars
                        }
                        List<Tag> tags = query.execute();
                        Log.d(TAG, "Tag in local db: " + tags.size() + "/" + tagLimit);
                        // If not enough tags locally, we get more on the server
                        if (tags.size() < tagLimit) {
                            super.load(term);
                        } else if (tags.size() > 0) {
                            manager.getSearchHistory().onSearchResponse(term, tags, true);
                        }
                    }
                });
        searchAndSelectTagManager.loadTags("");

        //set Initial Tags
        EventTag eventTag = IntentsUtils.extractEventTag(getIntent());
        if(eventTag != null) {
            searchAndSelectTagManager.addTag(eventTag.tag.getName());
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_validate_tags:
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


    private void postTags() {
        menu.findItem(R.id.action_validate_tags).setEnabled(false);
        eventEventPost.setTags(selectedTagsRV.getAdapter().getData());
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
                        menu.findItem(R.id.action_validate_tags).setEnabled(true);
                    }
                })
                .perform();
    }
}
