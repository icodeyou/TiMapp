package com.timappweb.timapp.data.loader;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.utils.loaders.ModelLoader;

import java.util.List;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * Created by stephane on 5/10/2016.
 */
public class SingleEntryLoaderCallback<DataType> implements LoaderManager.LoaderCallbacks<List<DataType>> {

    private static final String TAG = "SingleEntryLoaderCallb";
    private final int syncType;
    private WaveSwipeRefreshLayout mSwipeRefreshLayout = null;
    private Activity context;
    private int key;
    private Class<? extends SyncBaseModel> clazz;

    public SingleEntryLoaderCallback(Activity context,
                                     int key,
                                     Class<? extends SyncBaseModel> clazz,
                                     int syncType) {
        this.context = context;
        this.key = key;
        this.clazz = clazz;
        this.syncType = syncType;
    }

    @Override
    public Loader<List<DataType>> onCreateLoader(int id, Bundle args) {
        SyncBaseModel.getEntry(User.class, context, key, syncType);
        return new ModelLoader(context, User.class, SyncBaseModel.queryByRemoteId(clazz, key), true);
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
        SyncBaseModel.getRemoteEntry(User.class, context, key, syncType);
    }

    public void setSwipeAndRefreshLayout(WaveSwipeRefreshLayout swipeAndRefreshLayout) {
        this.mSwipeRefreshLayout = swipeAndRefreshLayout;

        mSwipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SingleEntryLoaderCallback.this.onRefresh();
            }
        });
    }

    public void setSwipeAndRefreshLayout() {
        this.setSwipeAndRefreshLayout((WaveSwipeRefreshLayout) context.findViewById(R.id.swipe_refresh_layout));
    }
}
