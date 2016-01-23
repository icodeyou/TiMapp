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
            if(newText.length()<2) {
                //if this action is removed, onQueryTextChange will not be called
                // the second time the spacebar is pressed.
                TagActivity tagActivity = (TagActivity) manager.getActivity();
                tagActivity.simulateKey();
            }
            newText = newText.substring(0, newText.length()-1);
            onQueryTextSubmit(newText);
        }
        else {
            manager.suggestTag(newText);
        }
        return false;
    }
}
