package com.timappweb.timapp.listeners;

import com.timappweb.timapp.activities.AddTagActivity;

public class OnAddTagListener extends OnBasicQueryTagListener {

    private final AddTagActivity addTagActivity;

    public OnAddTagListener(AddTagActivity addTagActivity) {
        super();
        this.addTagActivity = addTagActivity;
    }

    @Override
    public void addTag(String query) {
        manager.addTag(query);
        addTagActivity.actionCounter();
    }
}