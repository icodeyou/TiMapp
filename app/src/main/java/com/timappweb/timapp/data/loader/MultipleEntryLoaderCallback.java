package com.timappweb.timapp.data.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.activeandroid.query.From;
import com.timappweb.timapp.data.models.MyModel;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.utils.loaders.ModelLoader;

import java.util.List;

/**
 * Created by stephane on 5/10/2016.
 */
public class MultipleEntryLoaderCallback<DataType> implements LoaderManager.LoaderCallbacks<List<DataType>>, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MultipleEntriesCallback";

    // ---------------------------------------------------------------------------------------------

    protected final Class<? extends MyModel>  clazz;
    protected       From                            query;
    protected       SyncAdapterOption               syncOption;
    private         SwipeRefreshLayout              mSwipeRefreshLayout;
    /**
     * Min delay between two sync with the server sync delay
     */
    private         long                            syncDelay;
    protected       Context                         context;

    // ---------------------------------------------------------------------------------------------

    public MultipleEntryLoaderCallback(Context context,
                                       long syncDelay,
                                       int syncType,
                                       From query,
                                        Class<? extends MyModel> clazz) {
        this.context = context;
        this.syncOption = new SyncAdapterOption(syncType);
        this.syncDelay = syncDelay;
        this.query = query;
        this.clazz = clazz;
    }

    public MultipleEntryLoaderCallback(Context context,
                                       long syncDelay,
                                       int syncType,
                                       Class<? extends SyncBaseModel> clazz) {
        this(context, syncDelay, syncType, null, clazz);
    }

    // ---------------------------------------------------------------------------------------------

    public void setSyncDelay(long syncDelay) {
        this.syncDelay = syncDelay;
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
        //fetchEntries(true);
    }

    public void setSwipeAndRefreshLayout(SwipeRefreshLayout swipeAndRefreshLayout, boolean setOnRefreshCallback) {
        this.mSwipeRefreshLayout = swipeAndRefreshLayout;

        if (setOnRefreshCallback){
            mSwipeRefreshLayout.setOnRefreshListener(this);
        }
    }

    public void setSwipeAndRefreshLayout(SwipeRefreshLayout swipeAndRefreshLayout) {
        setSwipeAndRefreshLayout(swipeAndRefreshLayout, true);
    }


    public long getSyncDelay() {
        return syncDelay;
    }

    public int getSyncType() {
        return this.syncOption.getSyncType();
    }
}
