package com.timappweb.timapp.listeners;

public class OnSuggestQueryListener extends OnBasicQueryTagListener {

    public OnSuggestQueryListener() {
        super();
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(super.onQueryTextChange(newText)) {
            manager.suggestTag(newText);
        }
        return true;
    }
}