package com.timappweb.timapp.listeners;

import com.timappweb.timapp.activities.AddTagActivity;

public class OnAddTagListener extends OnBasicQueryTagListener {

    private final AddTagActivity addTagActivity;

    public OnAddTagListener(AddTagActivity addTagActivity) {
        super();
        this.addTagActivity = addTagActivity;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        super.onQueryTextSubmit(query);
        addTagActivity.actionCounter();
        return true;
    }

    @Override
    public void addTag(String query) {
        super.addTag(query);
        addTagActivity.actionCounter();
    }
}