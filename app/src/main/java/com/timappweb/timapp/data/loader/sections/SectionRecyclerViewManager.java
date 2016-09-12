package com.timappweb.timapp.data.loader.sections;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.ProgressItem;
import com.timappweb.timapp.data.loader.RecyclerViewManager;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;

import java.io.IOException;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;

import com.timappweb.timapp.views.SwipeRefreshLayout;

/**
 * Created by Stephane on 06/09/2016.
 */
public class SectionRecyclerViewManager
        extends RecyclerViewManager<SectionRecyclerViewManager>
        implements SectionDataLoader.Callback {

    private final SectionDataLoader mDataLoader;

    public SectionRecyclerViewManager(Context context, MyFlexibleAdapter adapter, SectionDataLoader dataLoader) {
        super(context, adapter);
        this.mDataLoader = dataLoader;
        this.mDataLoader.setCallback(this);
    }

    protected SectionDataLoader.Callback mCallback;
    public SectionRecyclerViewManager setCallback(SectionDataLoader.Callback callback){
        this.mCallback = callback;
        return this;
    }

    @Override
    public void onLoadMore() {
        if (mDataLoader.isLoading())
            return;
        if (!mDataLoader.loadMore()) {
            setRefreshing(false);
            mAdapter.removeProgressItem();
        }
    }

    @Override
    public void onRefresh() {
        if (mDataLoader.isLoading())
            return;
        if (!mDataLoader.loadNewest()){
            setRefreshing(false);
            mAdapter.removeProgressItem();
        }
    }

    @Override
    public void onLoadEnd(SectionContainer.PaginatedSection section, List data) {
        setRefreshing(false);

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

        //this.updateNoDataView();
    }

    @Override
    public void onLoadError(Throwable error, SectionContainer.PaginatedSection section) {
        setRefreshing(false);
        mAdapter.removeProgressItem();

        if (section.getLoadType() == SectionDataLoader.LoadType.MORE){
            mAdapter.onLoadMoreComplete(null);
        }
        //this.updateNoDataView();

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

    public void firstLoad() {
        if (mDataLoader.isLoading())
            return;
        if (mDataLoader.firstLoad()){
            setRefreshing(false);
        }
    }

}
