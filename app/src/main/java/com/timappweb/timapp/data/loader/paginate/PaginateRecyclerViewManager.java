package com.timappweb.timapp.data.loader.paginate;

import android.content.Context;
import android.widget.Toast;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddSpotActivity;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.data.loader.RecyclerViewManager;
import com.timappweb.timapp.data.loader.sections.SectionContainer;
import com.timappweb.timapp.data.loader.sections.SectionDataLoader;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;

import java.io.IOException;
import java.util.List;

/**
 * Created by Stephane on 06/09/2016.
 */
public class PaginateRecyclerViewManager
        extends RecyclerViewManager<PaginateRecyclerViewManager>
        implements PaginateDataLoader.Callback {

    private final PaginateDataLoader mDataLoader;
    private PaginateDataLoader.Callback mCallback;

    public PaginateRecyclerViewManager(Context context, MyFlexibleAdapter adapter, PaginateDataLoader dataLoader) {
        super(context, adapter);
        this.mDataLoader = dataLoader;
        this.mDataLoader.setCallback(this);
    }


    @Override
    public void onLoadMore() {
        if (mDataLoader.isLoading())
            return;
        if (!mDataLoader.loadNextPage()) {
            setRefreshing(false);
            mAdapter.removeProgressItem();
        }
    }

    @Override
    public void onRefresh() {
        if (mDataLoader.isLoading())
            return;

        mDataLoader.clear();
        if (!mDataLoader.loadNextPage()){
            setRefreshing(false);
        }
    }

    @Override
    public void onLoadEnd(PaginateDataLoader.PaginateRequestInfo info, List data) {
        setRefreshing(false);

        List items = mItemTransformer.transform(data);
        mAdapter.onLoadMoreComplete(items);

        if (this.mCallback != null){
            this.mCallback.onLoadEnd(info, data);
        }
         //this.updateNoDataView();
    }

    @Override
    public void onLoadError(Throwable error, PaginateDataLoader.PaginateRequestInfo info) {
        setRefreshing(false);
        //this.updateNoDataView();

        if (this.mCallback != null){
            this.mCallback.onLoadError(error, info);
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

    public PaginateRecyclerViewManager setCallback(AddSpotActivity callback) {
        this.mCallback = callback;
        return this;
    }
}
