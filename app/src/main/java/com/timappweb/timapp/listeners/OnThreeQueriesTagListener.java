package com.timappweb.timapp.listeners;

import com.timappweb.timapp.activities.TagActivity;

public class OnThreeQueriesTagListener extends OnBasicQueryTagListener {

    private final TagActivity tagActivity;

    public OnThreeQueriesTagListener(TagActivity tagActivity) {
        super();
        this.tagActivity = tagActivity;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        super.onQueryTextSubmit(query);
        tagActivity.actionCounter();
        return true;
    }

    @Override
    public void addTag(String query) {
        super.addTag(query);
        tagActivity.actionCounter();
    }
}