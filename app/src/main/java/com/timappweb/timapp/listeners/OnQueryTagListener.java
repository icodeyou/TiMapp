package com.timappweb.timapp.listeners;

import android.support.v7.widget.SearchView;

import com.timappweb.timapp.managers.SearchAndSelectTagManager;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;

public class OnQueryTagListener implements SearchView.OnQueryTextListener {

    private final SearchAndSelectTagManager manager;

    public OnQueryTagListener(SearchAndSelectTagManager searchAndSelectTagManager) {
        this.manager = searchAndSelectTagManager;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        manager.addTag(query);
        manager.getSearchView().setIconified(true);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        manager.suggestTag(newText);
        return false;
    }
}
