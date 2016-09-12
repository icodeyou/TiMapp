package com.timappweb.timapp.data.loader;

import android.content.Context;
import android.view.View;

import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.ProgressItem;
import com.timappweb.timapp.data.loader.sections.SectionDataLoader;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import java.util.LinkedList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;

/**
 * Created by Stephane on 12/09/2016.
 */
public abstract class RecyclerViewManager<This>
        implements FlexibleAdapter.EndlessScrollListener, SwipeRefreshLayout.OnRefreshListener{
    private static final int            ENDLESS_SCROLL_THRESHOLD        = 1;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private long minDelayAutoRefresh;
    private long minDelayForceRefresh;
    private View mNoDataView;

    protected RecyclerViewManager.ItemTransformer mItemTransformer;
    protected final MyFlexibleAdapter mAdapter;
    protected final Context mContext;


    public RecyclerViewManager(Context context, MyFlexibleAdapter adapter) {
        this.mAdapter = adapter;
        this.mContext = context;
    }

    public void setRefreshing(boolean state){
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(state);
    }

    /*
    protected void updateNoDataView() {
        if (this.mNoDataView != null){
            mNoDataView.setVisibility(mAdapter.hasData()
                    ? View.GONE
                    : View.VISIBLE);
        }
    }*/

    public This setNoDataView(final View noDataView) {
        mAdapter.initializeListeners(new FlexibleAdapter.OnUpdateListener() {
            @Override
            public void onUpdateEmptyView(int size) {
                noDataView.setVisibility(size == 0 ? View.VISIBLE : View.GONE);
            }
        });
        this.mNoDataView = noDataView;
        return (This) this;
    }

    public This setMinDelayAutoRefresh(long minDelayAutoRefresh) {
        this.minDelayAutoRefresh = minDelayAutoRefresh;
        return (This) this;
    }

    public This setMinDelayForceRefresh(long minDelayForceRefresh) {
        this.minDelayForceRefresh = minDelayForceRefresh;
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
