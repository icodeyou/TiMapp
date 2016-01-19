package com.timappweb.timapp.managers;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;

import com.greenfrvr.hashtagview.HashtagView;
import com.timappweb.timapp.activities.PublishActivity;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.listeners.OnQueryTagListener;
import com.timappweb.timapp.listeners.RecyclerItemTouchListener;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.SearchHistory;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Response;

public class SearchAndSelectTagManager {

    private static final String TAG = "SearchAndSelectTag";
    private static final int MINIMAL_SEARCH_LENGTH = 2;


    private Activity activity;
    private SearchView searchView;
    private HorizontalTagsRecyclerView selectedTagsRecyclerView;
    private HashtagView suggestedRecyclerView;
    private SearchHistory<Tag> searchHistory;

    private int remaining_tags;

    public SearchAndSelectTagManager(Activity activity,
                                     SearchView searchView,
                                     HashtagView suggestedRecyclerView,
                                     HorizontalTagsRecyclerView selectedRecyclerView) {
        this.activity = activity;
        this.searchView = searchView;
        this.suggestedRecyclerView = suggestedRecyclerView;
        this.selectedTagsRecyclerView = selectedRecyclerView;
        this.init();
        this.loadTags("");
    }

    private void init(){

        // Source de listener à double implémentation que tu trouves bizarre que tu veux enlever :
        // http://stackoverflow.com/questions/24471109/recyclerview-onclick
        /*
        suggestedRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(activity, new RecyclerItemTouchListener.OnItemClickListener() {

            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int position) {
                Log.d(TAG, "Clicked on suggested item");
                FilledTagsAdapter suggestedAdapter = (FilledTagsAdapter) recyclerView.getAdapter();
                String selectedTag = suggestedAdapter.getData(position).getName();
                selectedTagsRecyclerView.getAdapter().addData(selectedTag);
                selectedTagsRecyclerView.scrollToEnd();
            }

        }));
*/
        resetCounter();
        setCounterHint();

        suggestedRecyclerView.addOnTagClickListener(new HashtagView.TagsClickListener() {
            @Override
            public void onItemClicked(Object item) {
                //TODO: add tag
                decreaseCounter();
                setCounterHint();
            }
        });

        selectedTagsRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(activity, new RecyclerItemTouchListener.OnItemClickListener() {

            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int position) {
                Log.d(TAG, "Clicked on selected item");
                selectedTagsRecyclerView.getAdapter().removeData(position);
                increaseCounter();
                setCounterHint();
            }

        }));

        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);

        SearchView.OnQueryTextListener queryTextListener =
                new OnQueryTagListener(this);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setOnQueryTextListener(queryTextListener);

        this.searchHistory = new SearchHistory<>();
    }


    public void decreaseCounter() {
        remaining_tags = remaining_tags-1;
    }

    public void increaseCounter() {
        remaining_tags = remaining_tags+1;
    }

    public void resetCounter() {
        remaining_tags = 3;
    }

    public void setCounterHint() {
        switch (remaining_tags) {
            case 3:
                searchView.setQueryHint("choose 3 tags");
                break;
            case 2:
                searchView.setQueryHint("Choose 2 tags");
                break;
            case 1:
                searchView.setQueryHint("One more !");
                break;
            case 0:
                //Save data
                 ArrayList<String> finalTags = selectedTagsRecyclerView.getAdapter().getStringsFromTags();

                //Change activity
                Intent intent = new Intent(activity, PublishActivity.class);
                intent.putStringArrayListExtra("finalTags",finalTags);
                activity.startActivity(intent);

                //Clear list of tags in case back button is pressed in PublishActivity
                selectedTagsRecyclerView.getAdapter().resetData();
                resetCounter();
                setCounterHint();
            default:
                break;
        }
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
        // add data to adapter
        searchHistory.setLastSearch(term);

        // Data are in cache
        if (searchHistory.hasTerm(term)){
            suggestedRecyclerView.setData(searchHistory.get(term).getData());
        }
        else {
            // Data are not in cache, try searching for a sub term
            SearchHistory.Item subHistory = searchHistory.get(term);
            if (subHistory != null){
                suggestedRecyclerView.setData(subHistory.getData());
                if (subHistory.isComplete()){
                    return ;
                }
            }
            searchHistory.create(term);

            RestClient.service().suggest(term, new RestCallback<List<Tag>>(activity) {
                @Override
                public void success(List<Tag> tags, Response response) {
                    Log.d(TAG, "Got suggested tags from server with term " + term + "* : " + tags.size());
                    searchHistory.set(term, tags);
                    if (searchHistory.isLastSearch(term)) {
                        Log.d(TAG, "'" + term + "' is the last search, setting data");
                        suggestedRecyclerView.setData(tags);
                    }
                }

            });
        }
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

}