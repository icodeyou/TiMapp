package com.timappweb.timapp.listeners;

import android.support.v7.widget.SearchView;

import com.timappweb.timapp.activities.TagActivity;
import com.timappweb.timapp.managers.SearchAndSelectTagManager;

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
                tagActivity.simulateKeys();
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
