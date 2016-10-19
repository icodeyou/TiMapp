package com.timappweb.timapp.listeners;

import android.support.v7.widget.SearchView;

import com.timappweb.timapp.managers.SearchAndSelectTagManager;

public class OnBasicQueryTagListener implements SearchView.OnQueryTextListener {

    protected SearchAndSelectTagManager manager;

    public OnBasicQueryTagListener() {
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        addTag(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(newText.length() > 0 && newText.substring(newText.length()-1).equals(" ")) {
            onQueryTextSubmit(newText.trim());
        }
        else {
            manager.suggestTag(newText);
        }
        return true;
    }

    public void addTag(String query) {
        manager.addTag(query);
    }

    public void setSearchAndSelectTagManager(SearchAndSelectTagManager searchAndSelectTagManager) {
        this.manager = searchAndSelectTagManager;
    }
}
