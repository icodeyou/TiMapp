package com.timappweb.timapp.listeners;

import android.support.v7.widget.SearchView;

import com.timappweb.timapp.managers.SearchAndSelectTagManager;

public class OnBasicQueryTagListener implements SearchView.OnQueryTextListener {

    protected SearchAndSelectTagManager manager;

    String lastQuery;

    public OnBasicQueryTagListener() {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if(manager.addTag(query)) {
            manager.actionCounter();
        }
        return true; // must always return true to keep keyboard open
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //if spacebar is typed
        if(newText.length() > 0 && newText.substring(newText.length()-1).equals(" ")) {
            manager.setQuery(newText.trim(), true);
            //TODO : If no success when adding tag with a space, and we press space again. onQueryTextChange isn't called.
            //This is probably because onQueryTextChange is immediately called after the method setQuery above.
            //We should find a way to remove space from searchview without calling onQueryTextChange
            return false;
        }
        else {
            return true;
        }
    }

    public void setSearchAndSelectTagManager(SearchAndSelectTagManager searchAndSelectTagManager) {
        this.manager = searchAndSelectTagManager;
    }
}
