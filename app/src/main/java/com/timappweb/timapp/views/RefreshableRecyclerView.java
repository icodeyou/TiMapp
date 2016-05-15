package com.timappweb.timapp.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import java.security.InvalidParameterException;

/**
 * Created by stephane on 5/14/2016.
 */
public class RefreshableRecyclerView extends RecyclerView {

    private android.support.v4.widget.SwipeRefreshLayout mSwipeRefreshLayout;

    public RefreshableRecyclerView(Context context) {
        super(context);
    }

    public RefreshableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RefreshableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.init();
    }

    public void init(){
        this.mSwipeRefreshLayout = (android.support.v4.widget.SwipeRefreshLayout) getParent();
        if (this.mSwipeRefreshLayout == null) throw new InvalidParameterException("Refreshable recycler view should be wrap with a SwipeRefreshLayout");
        this.addOnScrollListener(new MyOnScrollListener());
    }

    private class MyOnScrollListener extends OnScrollListener {

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


}
