package com.timappweb.timapp.listeners;

import com.timappweb.timapp.activities.AddTagActivity;

public class OnThreeQueriesTagListener extends OnBasicQueryTagListener {

    private final AddTagActivity addTagActivity;

    public OnThreeQueriesTagListener(AddTagActivity addTagActivity) {
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