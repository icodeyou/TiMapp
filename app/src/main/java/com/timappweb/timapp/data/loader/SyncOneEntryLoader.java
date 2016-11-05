package com.timappweb.timapp.data.loader;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.property.BaseProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.tables.BaseTable;
import com.timappweb.timapp.utils.loaders.AutoModelLoader;

import java.util.List;

import com.timappweb.timapp.utils.loaders.SingleModelLoader;
import com.timappweb.timapp.views.SwipeRefreshLayout;

/**
 * Created by stephane on 5/10/2016.
 */
public class SyncOneEntryLoader<DataType> implements LoaderManager.LoaderCallbacks<DataType> {

    private static final String TAG = "SingleEntryLoaderCallb";
    private final int syncType;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private Activity context;
    private long key;
    private Class<? extends SyncBaseModel> clazz;
    private BaseProperty primaryKeyField;

    public SyncOneEntryLoader(Activity context,
                              long key,
                              Property<Long> primaryKeyField,
                              Class<? extends SyncBaseModel> clazz,
                              int syncType) {
        this.context = context;
        this.key = key;
        this.clazz = clazz;
        this.syncType = syncType;
        this.primaryKeyField = primaryKeyField;
    }

    @Override
    public Loader<DataType> onCreateLoader(int id, Bundle args) {
        SyncBaseModel.getEntry(User.class, primaryKeyField, context, key, syncType);
        return new SingleModelLoader(context, User.class, BaseTable.queryByRemoteId(clazz, this.primaryKeyField, key), true);
    }

    @Override
    public void onLoadFinished(Loader<DataType> loader, DataType data) {
        Log.d(TAG, "loaded finish");
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<DataType> loader) {

    }

    public void onRefresh(){
        Log.v(TAG, "Refreshing data");
        if (mSwipeRefreshLayout != null) mSwipeRefreshLayout.setRefreshing(true);
        BaseTable.syncEntry(User.class, context, key, syncType);
    }

    public void setSwipeAndRefreshLayout(SwipeRefreshLayout swipeAndRefreshLayout) {
        this.mSwipeRefreshLayout = swipeAndRefreshLayout;

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SyncOneEntryLoader.this.onRefresh();
            }
        });
    }

    public void setSwipeAndRefreshLayout() {
        this.setSwipeAndRefreshLayout((SwipeRefreshLayout) context.findViewById(R.id.swipe_refresh_layout));
    }
}
