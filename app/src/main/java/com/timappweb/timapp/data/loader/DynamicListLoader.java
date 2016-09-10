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
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * Created by Stephane on 06/09/2016.
 */
public class DynamicListLoader implements FlexibleAdapter.EndlessScrollListener, WaveSwipeRefreshLayout.OnRefreshListener, SectionDataLoader.Callback {

    private static final int            ENDLESS_SCROLL_THRESHOLD        = 1;

    private final MyFlexibleAdapter mAdapter;
    private final SectionDataLoader mDataLoader;
    private final Context mContext;
    private SectionDataLoader.Callback mCallback;
    private WaveSwipeRefreshLayout mSwipeRefreshLayout;
    private long minDelayAutoRefresh;
    private long minDelayForceRefresh;
    private ItemTransformer mItemTransformer;

    public DynamicListLoader setNoDataView(View noDataView) {
        this.mNoDataView = noDataView;
        return this;
    }

    private View mNoDataView;

    public DynamicListLoader(Context context, MyFlexibleAdapter adapter, SectionDataLoader dataLoader) {
        this.mAdapter = adapter;
        this.mDataLoader = dataLoader;
        this.mDataLoader.setCallback(this);
        this.mContext = context;
    }


    public DynamicListLoader setItemTransformer(ItemTransformer transformer){
        this.mItemTransformer = transformer;
        return this;
    }

    public DynamicListLoader setCallback(SectionDataLoader.Callback callback){
        this.mCallback = callback;
        return this;
    }

    public DynamicListLoader setSwipeRefreshLayout(WaveSwipeRefreshLayout mSwipeRefreshLayout) {
        this.mSwipeRefreshLayout = mSwipeRefreshLayout;
        this.mSwipeRefreshLayout.setOnRefreshListener(this);
        return this;
    }

    public DynamicListLoader setEndlessScrollListener() {
        mAdapter.setEndlessScrollListener(this, new ProgressItem());
        mAdapter.setEndlessScrollThreshold(ENDLESS_SCROLL_THRESHOLD);

        return this;
    }

    @Override
    public void onLoadMore() {
        if (!mDataLoader.loadMore()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        if (!mDataLoader.loadNewest()){
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public DynamicListLoader setMinDelayAutoRefresh(long minDelayAutoRefresh) {
        this.minDelayAutoRefresh = minDelayAutoRefresh;
        return this;
    }

    public DynamicListLoader setMinDelayForceRefresh(long minDelayForceRefresh) {
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
