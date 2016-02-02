package com.timappweb.timapp.managers;

import android.app.Activity;
import android.util.Log;

import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.SearchHistory;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by stephane on 2/2/2016.
 */

public class SearchTagDataProvider implements SearchHistory.DataProvider<Tag> {
    private static final String TAG = "SearchHistoryDataProv";

    public void setManager(SearchAndSelectTagManager manager) {
        this.manager = manager;
    }

    private SearchAndSelectTagManager manager;

    public SearchTagDataProvider(SearchAndSelectTagManager manager) {
        this.manager = manager;
    }

    public SearchTagDataProvider() {

    }

    @Override
    public void load(final String term) {

        Call<List<Tag>> call = RestClient.service().suggest(term);
        call.enqueue(new RestCallback<List<Tag>>(manager.getActivity()) {
            @Override
            public void onResponse(Response<List<Tag>> response) {
                super.onResponse(response);
                if (response.isSuccess()){
                    List<Tag> tags = response.body();
                    Log.d(TAG, "Got suggested tags from server with term " + term + "* : " + tags.size());
                    manager.getSearchHistory().addInCache(term, tags);
                }
                onLoadEnds();
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                onLoadEnds();
            }
        });
    }

    @Override
    public void onLoadEnds() {

    }

    @Override
    public void onSearchComplete(String term, List<Tag> tags) {
        if ( manager.getSearchHistory().isLastSearch(term)) {
            if (tags.size() == 0){
                tags.add(new Tag(term));
            }
            manager.setSuggestedData(tags);
        }
    }
}