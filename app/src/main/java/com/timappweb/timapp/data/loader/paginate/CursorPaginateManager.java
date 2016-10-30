package com.timappweb.timapp.data.loader.paginate;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.ExpandableHeaderItem;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.models.ProgressItem;
import com.timappweb.timapp.data.loader.BeforeLoadCallback;
import com.timappweb.timapp.data.loader.RecyclerViewManager;
import com.timappweb.timapp.data.models.MyModel;
import com.timappweb.timapp.utils.Util;

import java.util.List;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.ISectionable;

/**
 * Created by Stephane on 06/09/2016.
 */
public class CursorPaginateManager<DataType extends MyModel>
        extends RecyclerViewManager<CursorPaginateManager<DataType>>
        implements CursorPaginateDataLoader.Callback<DataType> {

    private static final int            ENDLESS_SCROLL_THRESHOLD        = 1;

    private static final String TAG = "CursorPaginateManager";
    private final CursorPaginateDataLoader<DataType, ?> mDataLoader;
    private long minDelayForceRefresh   = -1;
    private long lastRefresh = -1;
    private ExpandableHeaderItem expandableHeaderItem;
    private boolean clearOnRefresh = false;
    private CursorPaginateDataLoader.Callback<DataType> callback;
    private boolean activeEndlessScroll = false;
    //private BeforeLoadCallback beforeLoadCallback;

    //private long minDelayAutoRefresh    = -1;

    public CursorPaginateManager(Context context, MyFlexibleAdapter adapter, CursorPaginateDataLoader<DataType, ?> dataLoader) {
        super(context, adapter);
        this.mDataLoader = dataLoader;
        this.mDataLoader.setCallback(this);
    }

    public CursorPaginateManager<DataType> setSubSection(ExpandableHeaderItem expandableHeaderItem){
        this.expandableHeaderItem = expandableHeaderItem;
        return this;
    }

    public CursorPaginateManager<DataType> setMinDelayForceRefresh(long minDelayForceRefresh) {
        this.minDelayForceRefresh = minDelayForceRefresh;
        return this;
    }

    /*
    public CursorPaginateManager<DataType> setMinDelayAutoRefresh(long minDelayAutoRefresh) {
        this.minDelayAutoRefresh = minDelayAutoRefresh;
        return this;
    }*/


    // ---------------------------------------------------------------------------------------------

    public void refresh() {
        setRefreshing(true);
        if (clearOnRefresh){
            this.mDataLoader.deleteCache();
            this.mDataLoader.cacheInfo.reset();
            this.clearItems();
            this.mDataLoader.loadNext();
        }
        else{
            this.mDataLoader.update();
        }
    }

    @Override
    public void onRefresh() {
        if ( this.minDelayForceRefresh == -1 || this.lastRefresh == -1 || Util.isOlderThan(this.lastRefresh, this.minDelayForceRefresh)){
            this.refresh();
        }
        else{
            Log.d(TAG, "Data up to date. Last update was: " + (this.lastRefresh !=  -1 ? ((System.currentTimeMillis() - this.lastRefresh)/1000) + " " +
                    "seconds ago. Max delay: " + this.minDelayForceRefresh/1000 + " seconds" : " NEVER"));
            Toast.makeText(MyApplication.getApplicationBaseContext(), R.string.data_already_refresh, Toast.LENGTH_SHORT).show();
            setRefreshing(false);
        }
    }

    public void clearItems() {
        if (expandableHeaderItem != null){
            this.mAdapter.removeItems(expandableHeaderItem);
        }
        else{
            mAdapter.removeAll();
        }
    }

    @Override
    public void onLoadMore() {
        if (!this.mDataLoader.hasMoreData()){
            mAdapter.onLoadMoreComplete(null);
            mAdapter.removeProgressItem();
            return;
        }
        this.mDataLoader.loadNext();
    }

    @Override
    public void onLoadStart(CursorPaginateDataLoader.LoadType loadType) {
        switch (loadType){
            case NEXT:
                break;
            case UPDATE:
                this.lastRefresh = System.currentTimeMillis();
                setRefreshing(true);
                break;
            case PREV:
                setRefreshing(true);
                break;
        }
        if (this.callback != null) this.callback.onLoadStart(loadType);
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    public void onLoadEnd(CursorPaginateDataLoader.LoadInfo<DataType> data, CursorPaginateDataLoader.LoadType type, boolean overwrite) {
        List<AbstractFlexibleItem> items = null;
        if (data != null){
            items = mItemTransformer.transform(data.items);
        }

        if (overwrite){
            Log.i(TAG, "Removing already loaded data");
            this.clearItems();
        }

        switch (type){
            case NEXT:
                if (items != null) {
                    if (expandableHeaderItem != null){
                        for (AbstractFlexibleItem item: items){
                            mAdapter.addSubItem(expandableHeaderItem, (ISectionable) item);
                        }
                    }
                    else {
                        mAdapter.onLoadMoreComplete(items);
                        if (items.size() > 0 && !mDataLoader.hasMoreData()){
                            mAdapter.onLoadMoreComplete(null);
                        }
                    }
                }
                break;
            case UPDATE:
                if (items != null) {
                    int offset = 0;
                    for (AbstractFlexibleItem item : items) {
                        if (mAdapter.contains(item)) {
                            Log.d(TAG, "Updating existing item");
                            mAdapter.updateItem(item, null);
                        } else {
                            if (expandableHeaderItem != null){
                                mAdapter.addSubItem(expandableHeaderItem, (ISectionable) item);
                            }
                            else{
                                mAdapter.addBeginning(item, offset++);
                            }
                        }
                    }
                }
                break;
            case PREV:
                mAdapter.addBeginning(items);
                break;
        }
        setRefreshing(false);

        if (!mAdapter.hasData() && this.noDataCallback != null){
            this.noDataCallback.run();
        }
        else if (mAdapter.hasData() && this.onDataCallback != null){
            this.onDataCallback.run();
        }

        if (this.callback != null) this.callback.onLoadEnd(data, type, overwrite);

        if (this.activeEndlessScroll && mDataLoader.hasMoreData()){
            mAdapter.setEndlessScrollListener(this, new ProgressItem());
            mAdapter.setEndlessScrollThreshold(ENDLESS_SCROLL_THRESHOLD);
        }
        else if (!mDataLoader.hasMoreData()){
            mAdapter.removeProgressItem();
        }
        this.activeEndlessScroll = false;
    }

    @Override
    public void onLoadError(Throwable error, CursorPaginateDataLoader.LoadType loadType) {
        setRefreshing(false);
        mAdapter.onLoadMoreComplete(null);
         /*switch (loadType){
            case NEXT:
                break;
            case UPDATE:
                setRefreshing(false);
                break;
            case PREV:
                break;
        }*/
        if (this.callback != null) this.callback.onLoadError(error, loadType);
    }


    public CursorPaginateManager<DataType> load() {
        //if (this.mDataLoader.isFirstLoad()){
        setRefreshing(true);
        //}
        this.mDataLoader.loadNext();
        return this;
    }

    public CursorPaginateManager<DataType> setClearOnRefresh(boolean clearOnRefresh) {
        this.clearOnRefresh = clearOnRefresh;
        return this;
    }

    public CursorPaginateManager<DataType> setCallback(CursorPaginateDataLoader.Callback<DataType> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * We only set the endless scroll once the first load is done
     * @return
     */
    public CursorPaginateManager<DataType> enableEndlessScroll() {
        this.activeEndlessScroll = true;
        return this;
    }

    /*
    public void reloadFromLocal() {
        mAdapter.removeAll();
        mDataLoader.localLoad();
    }*/

    /*
    public CursorPaginateManager<DataType> setBeforeLoadCallback(BeforeLoadCallback beforeLoadCallback) {
        this.beforeLoadCallback = beforeLoadCallback;
        return this;
    }*/
}
