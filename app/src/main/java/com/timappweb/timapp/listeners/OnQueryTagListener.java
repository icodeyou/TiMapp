package com.timappweb.timapp.listeners;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.Menu;

import com.timappweb.timapp.activities.TagActivity;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;
import com.timappweb.timapp.adapters.HorizontalTagsAdapter;

public class OnQueryTagListener implements SearchView.OnQueryTextListener {

    private final SearchAndSelectTagManager manager;

    public OnQueryTagListener(SearchAndSelectTagManager searchAndSelectTagManager) {
        this.manager = searchAndSelectTagManager;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        TagActivity tagActivity = (TagActivity) manager.getActivity();
        tagActivity.addTag(query);
        manager.getSearchView().setIconified(true);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(newText.contains(" ")) {
            newText = newText.substring(0, newText.length()-1);
            onQueryTextSubmit(newText);
            manager.getSearchView().setQuery("", false);
            TagActivity tagActivity = (TagActivity) manager.getActivity();
            tagActivity.simulateKey();
        }
        else {
            manager.suggestTag(newText);
        }
        return false;
    }
}
