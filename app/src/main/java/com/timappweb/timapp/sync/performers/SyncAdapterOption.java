package com.timappweb.timapp.sync.performers;

import android.os.Bundle;

import com.timappweb.timapp.data.models.SyncHistory;
import com.timappweb.timapp.sync.DataSyncAdapter;

/**
 * Created by stephane on 5/12/2016.
 */
public class SyncAdapterOption {

    private Bundle bundle;

    public SyncAdapterOption(int type) {
        bundle = new Bundle();
        setType(type);
    }

    public void setType(int type){
        bundle.putInt(DataSyncAdapter.SYNC_TYPE_KEY, type);
    }
    public int getSyncType() {
        return bundle.getInt(DataSyncAdapter.SYNC_TYPE_KEY);
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setLastSyncTime() {
        bundle.putLong(DataSyncAdapter.SYNC_LAST_TIME, SyncHistory.getLastSyncTime(getSyncType()));
    }

}
