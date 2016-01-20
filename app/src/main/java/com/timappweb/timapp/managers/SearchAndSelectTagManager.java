package com.timappweb.timapp.managers;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.greenfrvr.hashtagview.HashtagView;
import com.timappweb.timapp.activities.PublishActivity;
import com.timappweb.timapp.activities.TagActivity;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.listeners.OnQueryTagListener;
import com.timappweb.timapp.listeners.RecyclerItemTouchListener;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.IntentsUtils;
import com.timappweb.timapp.utils.SearchHistory;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit.client.Response;

public class SearchAndSelectTagManager {

    private static final String TAG = "SearchAndSelectTag";
    private static final int MINIMAL_SEARCH_LENGTH = 0;
    private static final int MAXIMAL_RESULT_SIZE = 40;


    private Activity activity;
    private SearchView searchView;
    private HorizontalTagsRecyclerView selectedTagsRecyclerView;
    private HashtagView suggestedRecyclerView;
    private SearchHistory searchHistory;
    private HorizontalTagsAdapter horizontalAdapter;

    private int remaining_tags;

    public SearchAndSelectTagManager(Activity activity,
                                     SearchView searchView,
                                     HashtagView suggestedRecyclerView,
                                     HorizontalTagsRecyclerView selectedRecyclerView) {
        this.activity = activity;
        this.searchView = searchView;
        this.suggestedRecyclerView = suggestedRecyclerView;
        this.selectedTagsRecyclerView = selectedRecyclerView;
        this.horizontalAdapter = (HorizontalTagsAdapter) selectedTagsRecyclerView.getAdapter();
        this.init();
        this.loadTags("");
    }

    private void init(){

        suggestedRecyclerView.addOnTagClickListener(new HashtagView.TagsClickListener() {
            @Override
            public void onItemClicked(Object item) {
                Tag tag = (Tag) item;
                suggestedRecyclerView.removeItem(item);
                addTag(tag.name);
            }
        });

        selectedTagsRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(activity, new RecyclerItemTouchListener.OnItemClickListener() {

            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int position) {
                Log.d(TAG, "Clicked on selected item");
                horizontalAdapter.removeData(position);
                //setCounterHint();
            }

        }));

        final SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);

        SearchView.OnQueryTextListener queryTextListener =
                new OnQueryTagListener(this);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setOnQueryTextListener(queryTextListener);

        this.searchHistory = new SearchHistory<Tag>(MINIMAL_SEARCH_LENGTH, MAXIMAL_RESULT_SIZE);
        this.searchHistory.setDataProvider(new SearchHistory.DataProvider<Tag>() {

            @Override
            public void load(final String term) {
                RestClient.service().suggest(term, new RestCallback<List<Tag>>(activity) {
                    @Override
                    public void success(List<Tag> tags, Response response) {
                        TagActivity tagActivity = (TagActivity) activity;
                        tagActivity.getProgressBarView().setVisibility(View.GONE);
                        Log.d(TAG, "Got suggested tags from server with term " + term + "* : " + tags.size());

                        searchHistory.addInCache(term, tags);
                    }

                });
            }

            @Override
            public void onSearchComplete(String term, List<Tag> tags) {
                if (searchHistory.isLastSearch(term)) {
                    setData(tags);
                }
            }
        });
    }


    public void addTag(String tag) {
        horizontalAdapter.addData(tag);
        selectedTagsRecyclerView.scrollToEnd();
    }
    /**
     * Suggest tag according to user input
     * Cache results according to the term given
     * @param term
     */
    public void suggestTag(final String term){
        if (term.length() < MINIMAL_SEARCH_LENGTH){
            return;
        }
        this.loadTags(term);
    }

    private void loadTags(final String term){
        searchHistory.search(term);
    }

    private void setData(List<Tag> tags) {
        suggestedRecyclerView.setData(tags);
        horizontalAdapter.notifyDataSetChanged();
    }

    // Getters
    public Activity getActivity() {
        return activity;
    }

    public SearchView getSearchView() {
        return searchView;
    }

    public HorizontalTagsRecyclerView getSelectedTagsRecyclerView() {
        return selectedTagsRecyclerView;
    }

    public List<Tag> getSelectedTags() {
        return horizontalAdapter.getData();
    }

    public void resetSelectedTags() {
        horizontalAdapter.resetData();
    }
}