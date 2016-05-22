package com.timappweb.timapp.views.parallaxviewpager;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.views.RefreshableRecyclerView;

/**
 * Created by desmond on 1/6/15.
 */
public abstract class RecyclerViewFragment extends ScrollTabHolderFragment {

    protected RefreshableRecyclerView mRecyclerView;
    protected int mScrollY;

    protected abstract void setScrollOnLayoutManager(int scrollY);
    protected abstract void setupRecyclerView();


    protected void setRecyclerViewOnScrollListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mScrollY += dy;

                if (mScrollTabHolder != null) {
                    mScrollTabHolder.onRecyclerViewScroll(recyclerView, dx, dy, mScrollY, mPosition);
                }
            }
        });
    }

    @Override
    public void adjustScroll(int scrollHeight, int headerHeight) {
        if (mRecyclerView == null) return;

        mScrollY = headerHeight - scrollHeight;
        setScrollOnLayoutManager(mScrollY);
    }
}
