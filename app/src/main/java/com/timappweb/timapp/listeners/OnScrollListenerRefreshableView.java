package com.timappweb.timapp.listeners;

import com.timappweb.timapp.views.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;

import com.timappweb.timapp.views.SwipeRefreshLayout;

/**
 * Created by stephane on 5/23/2016.
 */

public class OnScrollListenerRefreshableView extends RecyclerView.OnScrollListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;

    public OnScrollListenerRefreshableView(SwipeRefreshLayout swipeRefreshLayout) {
        mSwipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        //super.onScrollStateChanged(recyclerView, newState);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        //super.onScrolled(recyclerView, dx, dy);
        if (dx == 0)
            mSwipeRefreshLayout.setEnabled(true);
        else
            mSwipeRefreshLayout.setEnabled(false);
    }
}