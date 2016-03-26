package com.timappweb.timapp.listeners;

import com.timappweb.timapp.activities.FilterActivity;
import com.timappweb.timapp.activities.TagActivity;

public class OnFilterQueryTagListener extends OnBasicQueryTagListener {

    private final FilterActivity filterActivity;

    public OnFilterQueryTagListener(FilterActivity filterActivity) {
        super();
        this.filterActivity = filterActivity;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        super.onQueryTextSubmit(query);
        filterActivity.submit();
        return true;
    }

    @Override
    public void addTag(String query) {
        super.addTag(query);
        //filterActivity.setTextButton();
        filterActivity.setTopRvVisibility();
    }
}