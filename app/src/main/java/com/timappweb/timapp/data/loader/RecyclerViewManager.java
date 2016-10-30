package com.timappweb.timapp.data.loader;

import android.content.Context;
import android.view.View;

import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.ProgressItem;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import java.util.LinkedList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * Created by Stephane on 12/09/2016.
 */
public abstract class RecyclerViewManager<This> implements
        FlexibleAdapter.EndlessScrollListener,
        SwipeRefreshLayout.OnRefreshListener{

    private static final int            ENDLESS_SCROLL_THRESHOLD        = 1;

    // ---------------------------------------------------------------------------------------------

    private SwipeRefreshLayout mSwipeRefreshLayout;
    protected RecyclerViewManager.ItemTransformer mItemTransformer;
    protected final MyFlexibleAdapter mAdapter;
    protected final Context mContext;
    protected Runnable noDataCallback;
    protected Runnable onDataCallback;


    public RecyclerViewManager(Context context, MyFlexibleAdapter adapter) {
        this.mAdapter = adapter;
        this.mContext = context;
    }


    public void setRefreshing(final boolean state){
        if (mSwipeRefreshLayout != null) {
            //if (state){
                // hack otherwise the refresh view is not shown
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(state);
                    }
                });
            //}
            //else{
            //    mSwipeRefreshLayout.setRefreshing(state);
            //}
        }
    }


    public This setNoDataView(final View noDataView) {
        this.noDataCallback = new Runnable() {
            @Override
            public void run() {
                noDataView.setVisibility(View.VISIBLE);
            }
        };
        this.onDataCallback = new Runnable(){
            @Override
            public void run() {
                noDataView.setVisibility(View.GONE);
            }
        };
        return (This) this;
    }

    public This setOnNoDataCallback(Runnable callback) {
        this.noDataCallback = callback;
        return (This) this;
    }

    public This setItemTransformer(RecyclerViewManager.ItemTransformer transformer){
        this.mItemTransformer = transformer;
        return (This) this;
    }

    public This setSwipeRefreshLayout(SwipeRefreshLayout mSwipeRefreshLayout) {
        this.mSwipeRefreshLayout = mSwipeRefreshLayout;
        this.mSwipeRefreshLayout.setOnRefreshListener(this);
        return (This) this;
    }

    public This enableEndlessScroll() {
        mAdapter.setEndlessScrollListener(this, new ProgressItem());
        mAdapter.setEndlessScrollThreshold(ENDLESS_SCROLL_THRESHOLD);
        return (This) this;
    }


    // ---------------------------------------------------------------------------------------------

    public static abstract class ItemTransformer<T> {

        public List<AbstractFlexibleItem> transform(List<T> data){
            List<AbstractFlexibleItem> items = new LinkedList<>();
            if (data != null){
                for (T model : data) {
                    items.add(this.createItem(model));
                }
            }
            return items;
        }

        public abstract AbstractFlexibleItem createItem(T data);

    }
}
