package com.timappweb.timapp.managers;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.support.v7.widget.SearchView;

import com.greenfrvr.hashtagview.HashtagView;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.listeners.OnQueryTagListener;
import com.timappweb.timapp.utils.SearchHistory;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.List;


public class SearchAndSelectTagManager {

    private static final String TAG = "SearchAndSelectTag";

    private Activity activity;
    private SearchView searchView;
    private HorizontalTagsRecyclerView selectedTagsRecyclerView;
    private HashtagView suggestedRecyclerView;
    private SearchHistory searchHistory;
    private HorizontalTagsAdapter horizontalAdapter;

    public SearchAndSelectTagManager(Activity activity,
                                     SearchView searchView,
                                     HashtagView suggestedRecyclerView,
                                     HorizontalTagsRecyclerView selectedRecyclerView, SearchTagDataProvider searchTagDataProvider) {
        this.activity = activity;
        this.searchView = searchView;
        this.suggestedRecyclerView = suggestedRecyclerView;
        this.selectedTagsRecyclerView = selectedRecyclerView;
        this.horizontalAdapter = (HorizontalTagsAdapter) selectedTagsRecyclerView.getAdapter();
        this.init();
        this.setDataProvider(searchTagDataProvider);
        this.loadTags("");
    }

    public void setDataProvider(SearchTagDataProvider provider){
        provider.setManager(this);
        this.searchHistory.setDataProvider(provider);
    }

    private void init(){

        final SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);

        OnQueryTagListener queryTagListener = new OnQueryTagListener(this);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setOnQueryTextListener(queryTagListener);

        this.searchHistory = new SearchHistory<Tag>(
                MyApplication.getServerConfig().tags_min_search_length,
                MyApplication.getServerConfig().tags_suggest_limit);

    }
    /**
     * Suggest tag according to user input
     * Cache results according to the term given
     * @param term
     */
    public void suggestTag(final String term){
        if (term.length() < MyApplication.getServerConfig().tags_min_search_length){
            return;
        }
        this.loadTags(term);
    }

    private void loadTags(final String term){
        searchHistory.search(term);
    }

    public void setSuggestedData(List<Tag> tags) {
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

    public HashtagView getSuggestedTagsRV() {
        return suggestedRecyclerView;
    }

    public SearchHistory getSearchHistory() {
        return searchHistory;
    }
}