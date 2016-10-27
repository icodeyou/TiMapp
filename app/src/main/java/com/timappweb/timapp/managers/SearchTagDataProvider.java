package com.timappweb.timapp.managers;

import android.util.Log;

import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.SearchHistory;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by stephane on 2/2/2016.
 */

public abstract class SearchTagDataProvider implements SearchHistory.DataProvider<Tag> {

    private static final String                 TAG             = "SearchHistoryDataProv";
    protected SearchAndSelectTagManager           manager;

    // ---------------------------------------------------------------------------------------------

    public void setManager(SearchAndSelectTagManager manager) {
        this.manager = manager;
    }

    public SearchTagDataProvider() {

    }

    @Override
    public void load(final String term) {
        Call<List<Tag>> call = RestClient.service().suggest(term);
        RestClient.buildCall(call)
                .onResponse(new HttpCallback<List<Tag>>() {
                    @Override
                    public void successful(List<Tag> tags) {
                        Log.d(TAG, "Got suggested tags from server with term " + term + "* : " + tags.size());
                        manager.getSearchHistory().onSearchResponse(term, tags, true);
                    }
                })
                .onFinally(new HttpCallManager.FinallyCallback() {
                    @Override
                    public void onFinally(Response response, Throwable error) {
                        SearchTagDataProvider.this.onLoadEnds();
                    }

                })
                .perform();
    }

    @Override
    public abstract void onLoadEnds();

    @Override
    public void onSearchComplete(String term, List<Tag> tags) {
        if ( manager.getSearchHistory().isLastSearch(term)) {
            if (!manager.hasSuggestedTag(term)){
                tags.add(new Tag(term));
            }
            manager.setSuggestedData(tags);
        }
    }
}