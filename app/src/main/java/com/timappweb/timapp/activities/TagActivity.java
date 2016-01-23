package com.timappweb.timapp.activities;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;

import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.greenfrvr.hashtagview.HashtagView;
import com.timappweb.timapp.adapters.DataTransformTag;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.listeners.RecyclerItemTouchListener;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;
import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.IntentsUtils;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.LinkedList;

public class TagActivity extends BaseActivity{

    private String TAG = "TagActivity";

    //Views
    private HorizontalTagsRecyclerView selectedTagsRV;
    private HashtagView suggestedTagsRV;
    private View progressBarView;
    private ListView placeListView;
    private Place currentPlace = null;

    // @Bind(R.id.hashtags1)
    protected HashtagView suggestedTagsView;

    //others
    private SearchAndSelectTagManager searchAndSelectTagManager;
    private View selectedTagsView;
    private Post currentPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check that we gave the place as an extra parameter
        this.currentPlace = IntentsUtils.extractPlace(getIntent());
        this.currentPost = IntentsUtils.extractPost(getIntent());
        if (this.currentPlace == null || this.currentPost == null){
            Log.d(TAG, "Place is null");
            IntentsUtils.addPostStepLocate(this);
            return;
        }
        
        setContentView(R.layout.activity_tag);
        this.initToolbar(false);

        //Initialize variables
        selectedTagsView = findViewById(R.id.rv_selected_tags);
        selectedTagsRV = (HorizontalTagsRecyclerView) selectedTagsView;
        suggestedTagsView = (HashtagView) findViewById(R.id.rv_search_suggested_tags);
        progressBarView = findViewById(R.id.progressbar_view);
        placeListView = (ListView) findViewById(R.id.place_lv);

        initAdapterPlace();

        setSelectedTagsViewGone();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (searchAndSelectTagManager != null){
            //Clear list of tags in case back button is pressed in PublishActivity
            searchAndSelectTagManager.resetSelectedTags();
            setCounterHint();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_tags, menu);

        setSearchview(menu);
        searchView.requestFocus();

        //set hint for searchview
        searchAndSelectTagManager = new SearchAndSelectTagManager(this,
                searchView, suggestedTagsView, selectedTagsRV);

        suggestedTagsRV = searchAndSelectTagManager.getSuggestedTagsRV();
        suggestedTagsRV.addOnTagClickListener(new HashtagView.TagsClickListener() {
            @Override
            public void onItemClicked(Object item) {
                Tag tag = (Tag) item;
                suggestedTagsRV.removeItem(item);
                addTag(tag.name);
            }
        });

        selectedTagsRV.addOnItemTouchListener(new RecyclerItemTouchListener(this, new RecyclerItemTouchListener.OnItemClickListener() {

            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int position) {
                Log.d(TAG, "Clicked on selected item");
                selectedTagsRV.getAdapter().removeData(position);
                setCounterHint();
            }
        }));

        suggestedTagsView.setData(new LinkedList<Tag>(), new DataTransformTag());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_validate:
                String query = searchView.getQuery().toString();
                if(query.isEmpty()) {
                    Toast.makeText(this, "Type a tag before submitting", Toast.LENGTH_SHORT).show();
                }
                else {
                    searchView.setQuery(query, true);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    //----------------------------------------------------------------------------------------------
    //Private methods

    private void initAdapterPlace() {
        PlacesAdapter placesAdapter = new PlacesAdapter(this);
        placesAdapter.add(currentPlace);
        placeListView.setAdapter(placesAdapter);
    }

    public void addTag(String tag) {
        selectedTagsRV.getAdapter().addData(tag);
        selectedTagsRV.scrollToEnd();
        setCounterHint();
    }

    public void setCounterHint() {
        switch (searchAndSelectTagManager.getSelectedTags().size()) {
            case 0:
                setSelectedTagsViewGone();
                searchView.setQueryHint("Choose 3 tags");
                break;
            case 1:
                setSelectedTagsViewVisible();
                searchView.setQueryHint("2 tags left");
                break;
            case 2:
                searchView.setQueryHint("One more !");
                break;
            case 3:
                currentPost.setTags(searchAndSelectTagManager.getSelectedTags());
                currentPost.place_id = this.currentPlace.id;
                IntentsUtils.addPostStepPublish(this, this.currentPlace, currentPost);
            default:
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    //Public methods
    public void setSelectedTagsViewGone() {
        findViewById(R.id.top_line_hrv).setVisibility(View.GONE);
        selectedTagsView.setVisibility(View.GONE);
        findViewById(R.id.bottom_line_hrv).setVisibility(View.GONE);
    }

    public void setSelectedTagsViewVisible() {
        findViewById(R.id.top_line_hrv).setVisibility(View.VISIBLE);
        selectedTagsView.setVisibility(View.VISIBLE);
        findViewById(R.id.bottom_line_hrv).setVisibility(View.VISIBLE);
    }

    public void simulateKey() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Instrumentation inst = new Instrumentation();
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_SPACE);
                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);
            }
        }).start();
    }


    //----------------------------------------------------------------------------------------------
    //GETTER and SETTERS

    public View getProgressBarView() {
        return progressBarView;
    }

    //----------------------------------------------------------------------------------------------
    //Miscellaneous
    public void testClick(View view) {
        Intent intent = new Intent(this,PublishActivity.class);
        startActivity(intent);
    }
}
