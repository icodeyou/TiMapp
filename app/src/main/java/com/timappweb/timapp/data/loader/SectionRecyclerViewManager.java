package com.timappweb.timapp.data.loader;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.ProgressItem;
import com.timappweb.timapp.data.loader.sections.SectionDataLoader;
import com.timappweb.timapp.data.loader.sections.SectionContainer;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import com.timappweb.timapp.views.SwipeRefreshLayout;

/**
 * Created by Stephane on 06/09/2016.
 */
public class SectionRecyclerViewManager implements FlexibleAdapter.EndlessScrollListener, SwipeRefreshLayout.OnRefreshListener, SectionDataLoader.Callback {

    private static final int            ENDLESS_SCROLL_THRESHOLD        = 1;

    private final MyFlexibleAdapter mAdapter;
    private final SectionDataLoader mDataLoader;
    private final Context mContext;
    private SectionDataLoader.Callback mCallback;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private long minDelayAutoRefresh;
    private long minDelayForceRefresh;
    private ItemTransformer mItemTransformer;

    public SectionRecyclerViewManager setNoDataView(View noDataView) {
        this.mNoDataView = noDataView;
        return this;
    }

    private View mNoDataView;

    public SectionRecyclerViewManager(Context context, MyFlexibleAdapter adapter, SectionDataLoader dataLoader) {
        this.mAdapter = adapter;
        this.mDataLoader = dataLoader;
        this.mDataLoader.setCallback(this);
        this.mContext = context;
    }


    public SectionRecyclerViewManager setItemTransformer(ItemTransformer transformer){
        this.mItemTransformer = transformer;
        return this;
    }

    public SectionRecyclerViewManager setCallback(SectionDataLoader.Callback callback){
        this.mCallback = callback;
        return this;
    }

    public SectionRecyclerViewManager setSwipeRefreshLayout(SwipeRefreshLayout mSwipeRefreshLayout) {
        this.mSwipeRefreshLayout = mSwipeRefreshLayout;
        this.mSwipeRefreshLayout.setOnRefreshListener(this);
        return this;
    }

    public SectionRecyclerViewManager enableEndlessScroll() {
        mAdapter.setEndlessScrollListener(this, new ProgressItem());
        mAdapter.setEndlessScrollThreshold(ENDLESS_SCROLL_THRESHOLD);
        return this;
    }

    @Override
    public void onLoadMore() {
        if (mDataLoader.isLoading())
            return;
        if (!mDataLoader.loadMore()) {
            mSwipeRefreshLayout.setRefreshing(false);
            mAdapter.removeProgressItem();
        }
    }

    @Override
    public void onRefresh() {
        if (mDataLoader.isLoading())
            return;
        if (!mDataLoader.loadNewest()){
            mSwipeRefreshLayout.setRefreshing(false);
            mAdapter.removeProgressItem();
        }
    }

    public SectionRecyclerViewManager setMinDelayAutoRefresh(long minDelayAutoRefresh) {
        this.minDelayAutoRefresh = minDelayAutoRefresh;
        return this;
    }

    public SectionRecyclerViewManager setMinDelayForceRefresh(long minDelayForceRefresh) {
        this.minDelayForceRefresh = minDelayForceRefresh;
        return this;
    }

    @Override
    public void onLoadEnd(SectionContainer.PaginatedSection section, List data) {
        mSwipeRefreshLayout.setRefreshing(false);

        if (this.mCallback != null){
            this.mCallback.onLoadEnd(section, data);
        }

        List items = mItemTransformer.transform(data);

        switch (section.getLoadType()) {
            case MORE:
                mAdapter.onLoadMoreComplete(items);
                break;
            case NEWEST:
                mAdapter.addBeginning(items);
                break;
            case UPDATE:
                // TODO
                break;
        }

        this.updateNoDataView();
    }

    @Override
    public void onLoadError(Throwable error, SectionContainer.PaginatedSection section) {
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter.removeProgressItem();

        if (section.getLoadType() == SectionDataLoader.LoadType.MORE){
            mAdapter.onLoadMoreComplete(null);
        }
        this.updateNoDataView();

        if (this.mCallback != null){
            this.mCallback.onLoadError(error, section);
        }


        if (error instanceof IOException) {
            Toast.makeText(mContext, R.string.no_internet_connection_message, Toast.LENGTH_LONG).show();
        } else if (error instanceof CannotSyncException) {
            Toast.makeText(mContext, ((CannotSyncException) error).getUserFeedback(), Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(mContext, R.string.error_server_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void updateNoDataView() {
        if (this.mNoDataView != null){
            mNoDataView.setVisibility(mAdapter.hasData()
                    ? View.GONE
                    : View.VISIBLE);
        }
    }

    public void firstLoad() {
        if (mDataLoader.isLoading())
            return;
        if (mDataLoader.firstLoad()){
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    public static abstract class ItemTransformer<T> {

        List<AbstractFlexibleItem> transform(List<T> data){
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
