package com.timappweb.timapp.data.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.activeandroid.query.From;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.sync.performers.SyncAdapterOption;
import com.timappweb.timapp.utils.loaders.ModelLoader;

import java.util.List;

/**
 * Created by stephane on 5/10/2016.
 */
public class MultipleEntryLoaderCallback<DataType> implements LoaderManager.LoaderCallbacks<List<DataType>> {

    private static final String TAG = "MultipleEntriesCallback";

    // ---------------------------------------------------------------------------------------------

    private final   long                            syncDelay;
    private final   Class<?>                        clazz;
    protected       From                            query;
    protected       SyncAdapterOption               syncOption;
    private         SwipeRefreshLayout              mSwipeRefreshLayout;
    protected       Context                         context;

    // ---------------------------------------------------------------------------------------------

    public MultipleEntryLoaderCallback(Context context,
                                       long syncDelay,
                                       int syncType,
                                       From query,
                                        Class<?> clazz) {
        this.context = context;
        this.syncOption = new SyncAdapterOption(syncType);
        this.syncDelay = syncDelay;
        this.query = query;
        this.clazz = clazz;
    }

    public MultipleEntryLoaderCallback(Context context,
                                       long syncDelay,
                                       int syncType,
                                       Class<?> clazz) {
        this(context, syncDelay, syncType, null, clazz);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     *
     */
    public void fetchEntries(boolean force){
        if (force){
            SyncBaseModel.getRemoteEntries(context, syncOption);
        }
        else{
            SyncBaseModel.getEntries(context, syncOption, query, syncDelay);
        }
    }

    @Override
    public Loader<List<DataType>> onCreateLoader(int id, Bundle args) {
        fetchEntries(false);
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(true);
        return new ModelLoader(context, clazz, query, false);
    }

    @Override
    public void onLoadFinished(Loader<List<DataType>> loader, List<DataType> data) {
        Log.v(TAG, "Load finish for: " + this);
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<List<DataType>> loader) {

    }

    public void onRefresh(){
        Log.v(TAG, "Refreshing data");
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(true);
        fetchEntries(true);
    }

    public void setSwipeAndRefreshLayout(SwipeRefreshLayout swipeAndRefreshLayout, boolean setOnRefreshCallback) {
        this.mSwipeRefreshLayout = swipeAndRefreshLayout;

        if (setOnRefreshCallback){
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    MultipleEntryLoaderCallback.this.onRefresh();
                }
            });
        }
    }

    public void setSwipeAndRefreshLayout(SwipeRefreshLayout swipeAndRefreshLayout) {
        setSwipeAndRefreshLayout(swipeAndRefreshLayout, true);
    }


}
