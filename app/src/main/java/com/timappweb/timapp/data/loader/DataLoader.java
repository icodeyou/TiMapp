package com.timappweb.timapp.data.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.Model;
import com.activeandroid.query.From;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.models.ProgressItem;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.SyncHistory;
import com.timappweb.timapp.events.SyncResultMessage;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * Created by Stephane on 18/08/2016.
 */
public abstract class DataLoader<EntityType> implements LoaderManager.LoaderCallbacks<List<EntityType>>, SwipeRefreshLayout.OnRefreshListener, FlexibleAdapter.EndlessScrollListener {

    private static final String TAG = "DataLoader";

    private long minDelayAutoRefresh = 3600 * 24 * 1000;     // Automatic refresh after one day
    private long minDelayForceRefresh = 30 * 1000;            // Must wait 30 sec before reload
    protected From localQuery;

    //public enum ActionType {REFRESH, LOAD_MORE, NEWEST, UPDATE}
// -----------------------------------------------------------------------------------------

    protected SwipeRefreshLayout                    mSwipeAndRefreshLayout;
    protected Context                               context;
    protected SyncHistory.HistoryItemInterface      historyItemInterface;
    private SyncAdapterOption                       syncOptions;
    private FlexibleAdapter                         adapter;

    // -----------------------------------------------------------------------------------------


    public DataLoader(Context context) {
        this.context = context;
        this.syncOptions = new SyncAdapterOption();
    }

    protected abstract Loader<List<EntityType>> buildModelLoader();


    @Override
    public void onRefresh() {
        if (!SyncHistory.requireUpdate(DataSyncAdapter.SYNC_TYPE_FRIENDS, historyItemInterface, minDelayForceRefresh)) {
            Log.d(TAG, "User already refreshed the page a few time ago. NO ACTION");
            Toast.makeText(context, R.string.data_already_refresh, Toast.LENGTH_SHORT).show();
            if (mSwipeAndRefreshLayout != null) mSwipeAndRefreshLayout.setRefreshing(false);
            return;
        }
        this.refresh();
    }

    @Override
    public Loader<List<EntityType>> onCreateLoader(int id, Bundle args) {
        if (SyncHistory.requireUpdate(DataSyncAdapter.SYNC_TYPE_FRIENDS, historyItemInterface, minDelayAutoRefresh)) {
            Log.d(TAG, "Require automatic refresh");
            this.newest();
        }

        return buildModelLoader();
    }


    @Override
    public void onLoaderReset(Loader<List<EntityType>> loader) {
        // TODO
        Log.d(TAG, "Reseting loader: " + loader);
    }

    @Override
    public void onLoadFinished(Loader<List<EntityType>> loader, List<EntityType> data) {
        Log.i(TAG, "Loaded " + data.size() + " friends for the user");
        if (mSwipeAndRefreshLayout == null || !mSwipeAndRefreshLayout.isRefreshing()){
            this.onLocalFinish(data);
            this.onFinish(data);
        }
    }


    public void onLocalFinish(List<EntityType> data){}
    public void onFinish(List<EntityType> data){}

    // ---------------------------------------------------------------------------------------------

    @Override
    public void onLoadMore() {
        int localOffset = this.getLocalOffset();
        //if (this.localQuery.offset(localOffset).count() > 0){
        //    Log.d(TAG, "Loading data from local db with offset: " + localOffset);
        //    List<EntityType> data = (List<EntityType>) this.localQuery.offset(localOffset).execute();
        //    this.onLoadFinished(null, data);
        // }
        //else{
            SyncAdapterOption option = syncOptions.clone()
                    .setDirection(SyncAdapterOption.SyncDirection.DOWN)
                    .setMaxId(getMinRemoteId() - 1);
            launchSync(option);
        //}
    }

    public void newest() {
        if (mSwipeAndRefreshLayout != null) mSwipeAndRefreshLayout.setRefreshing(true);
        SyncAdapterOption option = syncOptions.clone()
                .setDirection(SyncAdapterOption.SyncDirection.DOWN)
                .setMinId(getMaxRemoteId() + 1);
        launchSync(option);
    }

    public void refresh() {
        if (mSwipeAndRefreshLayout != null) mSwipeAndRefreshLayout.setRefreshing(true);
        SyncAdapterOption option = syncOptions.clone()
                .setDirection(SyncAdapterOption.SyncDirection.DOWN);
        launchSync(option);
    }

    public void update() {
        if (mSwipeAndRefreshLayout != null) mSwipeAndRefreshLayout.setRefreshing(true);
        SyncAdapterOption option = syncOptions.clone()
                .setDirection(SyncAdapterOption.SyncDirection.DOWN)
                .setMaxId(getMaxRemoteId())
                .setMinId(getMinRemoteId());
        launchSync(option);
    }

    private void launchSync(SyncAdapterOption option) {
        if (mSwipeAndRefreshLayout != null) mSwipeAndRefreshLayout.setRefreshing(true);
        SyncBaseModel.startSync(context, option);
    }

    // ---------------------------------------------------------------------------------------------

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncError(IOException ex){
        Toast.makeText(context, R.string.no_internet_connection_message, Toast.LENGTH_LONG).show();
        if (mSwipeAndRefreshLayout != null) mSwipeAndRefreshLayout.setRefreshing(false);
        onFinish(null);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncError(CannotSyncException ex){
        Toast.makeText(context, ex.getUserFeedback(), Toast.LENGTH_LONG).show();
        if (mSwipeAndRefreshLayout != null) mSwipeAndRefreshLayout.setRefreshing(false);
        onFinish(null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncSuccess(SyncResultMessage syncResult){
        if (mSwipeAndRefreshLayout != null) mSwipeAndRefreshLayout.setRefreshing(false);
        if (syncResult.countItems() == 0){
            adapter.onLoadMoreComplete(null);
        }

    }
    // ---------------------------------------------------------------------------------------------

    public DataLoader<EntityType> setSyncOptions(SyncAdapterOption options){
        this.syncOptions = options;
        if (this.historyItemInterface != null){
            this.syncOptions.setHashId(this.historyItemInterface);
        }
        return this;
    }
    public SyncHistory.HistoryItemInterface getHistoryItemInterface() {
        return historyItemInterface;
    }

    public long getMaxRemoteId() {
        return Integer.MAX_VALUE;
    }

    public long getMinRemoteId() {
        return 0;
    }

    public DataLoader setMinDelayAutoRefresh(long minDelayAutoRefresh) {
        this.minDelayAutoRefresh = minDelayAutoRefresh;
        return this;
    }

    public DataLoader<EntityType> setMinDelayForceRefresh(long minDelayForceRefresh) {
        this.minDelayForceRefresh = minDelayForceRefresh;
        return this;
    }

    public DataLoader<EntityType> setSwipeAndRefreshLayout(SwipeRefreshLayout mSwipeAndRefreshLayout) {
        this.mSwipeAndRefreshLayout = mSwipeAndRefreshLayout;
        mSwipeAndRefreshLayout.setOnRefreshListener(this);
        return this;
    }

    public DataLoader<EntityType> setHistoryItemInterface(SyncHistory.HistoryItemInterface historyItemInterface) {
        this.historyItemInterface = historyItemInterface;
        this.syncOptions.setHashId(historyItemInterface);
        return this;
    }

    public SyncAdapterOption getSyncOptions() {
        return syncOptions;
    }

    public DataLoader<EntityType> setEnlessLoading(@NonNull FlexibleAdapter adapter) {
        this.adapter = adapter;
        adapter.setEndlessScrollListener(this, new ProgressItem());
        return this;
    }

    public void setLocalQuery(From localQuery) {
        this.localQuery = localQuery;
    }

    public int getLocalOffset(){
        return 0;
    }
}
