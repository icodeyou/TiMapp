package com.timappweb.timapp.data.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.From;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.FriendsAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.ProgressItem;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.SyncHistory;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserFriend;
import com.timappweb.timapp.events.SyncResultMessage;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;
import com.timappweb.timapp.utils.loaders.AutoModelLoader;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * Created by Stephane on 18/08/2016.
 */
public class SyncDataLoader<EntityType, This>
        implements LoaderManager.LoaderCallbacks<List<EntityType>>, FlexibleAdapter.EndlessScrollListener, WaveSwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "DataLoader";

    private long minDelayAutoRefresh = 3600 * 24 * 1000;     // Automatic refresh after one day
    private long minDelayForceRefresh = 30 * 1000;            // Must wait 30 sec before reload
    protected From localQuery;

    //public enum ActionType {REFRESH, LOAD_MORE, NEWEST, UPDATE}
// -----------------------------------------------------------------------------------------

    protected WaveSwipeRefreshLayout mSwipeAndRefreshLayout;
    protected Context                               context;
    protected SyncHistory.HistoryItemInterface      historyItemInterface;
    protected SyncAdapterOption                       syncOptions;
    protected MyFlexibleAdapter                  adapter;
    private Callback<EntityType> callback;
    private Loader<List<EntityType>> modelLoader;

    // -----------------------------------------------------------------------------------------

    CacheEngine   cacheEngine;

    public CacheEngine getCacheEngine() {
        return cacheEngine;
    }

    public This setCacheEngine(CacheEngine cacheEngine) {
        this.cacheEngine = cacheEngine;
        return (This) this;
    }

    public Loader<List<EntityType>> getModelLoader() {
        return modelLoader;
    }

    public This setModelLoader(Loader<List<EntityType>> modelLoader) {
        this.modelLoader = modelLoader;
        return (This) this;
    }

    public SyncDataLoader(Context context) {
        this.context = context;
        this.syncOptions = new SyncAdapterOption();
    }


    protected Loader<List<EntityType>> buildModelLoader() {
        return this.modelLoader;
    }


    @Override
    public void onRefresh() {
        if (!SyncHistory.requireUpdate(this.getSyncOptions().getSyncType(), historyItemInterface, minDelayForceRefresh)) {
            Log.d(TAG, "User already refreshed the page a few time ago. NO ACTION");
            Toast.makeText(context, R.string.data_already_refresh, Toast.LENGTH_SHORT).show();
            if (mSwipeAndRefreshLayout != null) mSwipeAndRefreshLayout.setRefreshing(false);
            return;
        }
        this.refresh();
    }

    @Override
    public Loader<List<EntityType>> onCreateLoader(int id, Bundle args) {
        return buildModelLoader();
    }


    @Override
    public void onLoaderReset(Loader<List<EntityType>> loader) {
        // TODO
        Log.d(TAG, "Resetting loader: " + loader);
    }

    @Override
    public void onLoadFinished(Loader<List<EntityType>> loader, List<EntityType> data) {
        Log.i(TAG, "Loaded " + data.size() + " friends for the user");
        if (mSwipeAndRefreshLayout == null || !mSwipeAndRefreshLayout.isRefreshing()){
            if (callback != null) callback.onLoadEnd(data);
        }
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    public void onLoadMore() {
        int localOffset = this.getLocalOffset();
        //if (this.localQuery.offset(localOffset).countNewInvites() > 0){
        //    Log.d(TAG, "Loading data from local db with offset: " + localOffset);
        //    List<EntityType> data = (List<EntityType>) this.localQuery.offset(localOffset).execute();
        //    this.onLoadFinished(null, data);
        // }
        //else{
            SyncAdapterOption option = syncOptions.clone()
                    .setDirection(RestQueryParams.SyncDirection.DOWN)
                    .setMaxId(getMinRemoteId() - 1);
            launchSync(option);
        //}
    }

    public void newest() {
        if (mSwipeAndRefreshLayout != null) mSwipeAndRefreshLayout.setRefreshing(true);
        SyncAdapterOption option = syncOptions.clone()
                .setDirection(RestQueryParams.SyncDirection.DOWN)
                .setMinId(getMaxRemoteId() + 1);
        launchSync(option);
    }

    public void refresh() {
        if (mSwipeAndRefreshLayout != null) mSwipeAndRefreshLayout.setRefreshing(true);
        SyncAdapterOption option = syncOptions.clone()
                .setDirection(RestQueryParams.SyncDirection.DOWN);
        launchSync(option);
    }

    public void update() {
        if (mSwipeAndRefreshLayout != null) mSwipeAndRefreshLayout.setRefreshing(true);
        SyncAdapterOption option = syncOptions.clone()
                .setDirection(RestQueryParams.SyncDirection.DOWN)
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
    public void onSyncError(Exception ex){
        if (mSwipeAndRefreshLayout != null) mSwipeAndRefreshLayout.setRefreshing(false);
        if (ex instanceof IOException){
            Toast.makeText(context, R.string.no_internet_connection_message, Toast.LENGTH_LONG).show();
        }
        else if (ex instanceof CannotSyncException){
            Toast.makeText(context, ((CannotSyncException)ex).getUserFeedback(), Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(context, R.string.action_performed_not_successful, Toast.LENGTH_LONG).show();
        }
        if (callback != null) callback.onLoadError(ex);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncSuccess(SyncResultMessage syncResult){
        if (mSwipeAndRefreshLayout != null) mSwipeAndRefreshLayout.setRefreshing(false);
        if (syncResult.countItems() == 0 && adapter != null){
            adapter.onLoadMoreComplete(null);
        }

    }
    // ---------------------------------------------------------------------------------------------

    public This setSyncOptions(SyncAdapterOption options){
        this.syncOptions = options;
        if (this.historyItemInterface != null){
            this.syncOptions.setHashId(this.historyItemInterface);
        }
        return (This) this;
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

    public This setMinDelayAutoRefresh(long minDelayAutoRefresh) {
        this.minDelayAutoRefresh = minDelayAutoRefresh;
        return (This) this;
    }

    public This setMinDelayForceRefresh(long minDelayForceRefresh) {
        this.minDelayForceRefresh = minDelayForceRefresh;
        return (This) this;
    }

    public This setSwipeAndRefreshLayout(WaveSwipeRefreshLayout mSwipeAndRefreshLayout) {
        this.mSwipeAndRefreshLayout = mSwipeAndRefreshLayout;
        mSwipeAndRefreshLayout.setOnRefreshListener(this);
        return (This) this;
    }

    public This setHistoryItemInterface(SyncHistory.HistoryItemInterface historyItemInterface) {
        this.historyItemInterface = historyItemInterface;
        this.syncOptions.setHashId(historyItemInterface);
        return (This) this;
    }

    public SyncAdapterOption getSyncOptions() {
        return syncOptions;
    }

    public This setEnlessLoading(@NonNull MyFlexibleAdapter adapter) {
        this.adapter = adapter;
        adapter.setEndlessScrollListener(this, new ProgressItem());
        return (This) this;
    }

    public void setLocalQuery(From localQuery) {
        this.localQuery = localQuery;
    }

    public int getLocalOffset(){
        return 0;
    }

    public void setAdapter(FriendsAdapter adapter) {
        this.adapter = adapter;
    }

    public Callback<EntityType> getCallback() {
        return callback;
    }

    public This setCallback(Callback<EntityType> callback) {
        this.callback = callback;
        return (This) this;
    }
    // ---------------------------------------------------------------------------------------------

    /**
     *
     * @param <T>
     */
    public interface Callback<T>{

        void onLoadEnd(List<T> data);

        void onLoadError(Throwable error);

    }

    // ---------------------------------------------------------------------------------------------

    public interface CacheEngine{

        boolean contains(SyncAdapterOption options);

        From query(SyncAdapterOption options);

    }
}
