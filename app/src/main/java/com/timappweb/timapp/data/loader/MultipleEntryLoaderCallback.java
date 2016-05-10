package com.timappweb.timapp.data.loader;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.activeandroid.query.From;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.utils.loaders.ModelLoader;

import java.util.List;

/**
 * Created by stephane on 5/10/2016.
 */
public class MultipleEntryLoaderCallback<DataType> implements LoaderManager.LoaderCallbacks<List<DataType>> {

    private static final String TAG = "MultipleEntriesCallback";
    private final int syncType;
    private final long syncDelay;
    private final From query;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private Activity context;

    public MultipleEntryLoaderCallback(Activity context,
                                       int syncType,
                                       long syncDelay,
                                       From query) {
        this.context = context;
        this.syncType = syncType;
        this.syncDelay = syncDelay;
        this.query = query;
    }

    @Override
    public Loader<List<DataType>> onCreateLoader(int id, Bundle args) {
        SyncBaseModel.getEntries(context, query, syncType, syncDelay);
        return new ModelLoader(context, User.class, query, false);
    }

    @Override
    public void onLoadFinished(Loader<List<DataType>> loader, List<DataType> data) {
        Log.d(TAG, "loaded finish");
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<List<DataType>> loader) {

    }

    public void onRefresh(){
        Log.v(TAG, "Refreshing data");
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(true);
        SyncBaseModel.getRemoteEntries(context, syncType);
    }

    public void setSwipeAndRefreshLayout(SwipeRefreshLayout swipeAndRefreshLayout) {
        this.mSwipeRefreshLayout = swipeAndRefreshLayout;

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MultipleEntryLoaderCallback.this.onRefresh();
            }
        });
    }

    public void setSwipeAndRefreshLayout() {
        this.setSwipeAndRefreshLayout((SwipeRefreshLayout) context.findViewById(R.id.swipe_refresh_layout));
    }
}
