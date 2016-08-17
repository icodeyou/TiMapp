package com.timappweb.timapp.rest.callbacks;

import android.util.Log;

import com.activeandroid.query.From;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.sync.callbacks.RemoteMasterSyncCallback;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;

import java.util.List;

/**
 * Created by Stephane on 08/08/2016.
 */
public class RemoteMasterSyncHttpCallback<T extends SyncBaseModel> extends HttpCallback<List<T>>{

    private static final String TAG = "RemoteMasterSyncCB";
    private final From localData;

    public RemoteMasterSyncHttpCallback(Class<T> clazz, From from) {
        Log.i(TAG, "Performing model sync for " + clazz.getCanonicalName() + "...");
        this.localData = from;
    }

    @Override
    public void successful(List<T> remoteEntries) {
        new MultipleEntriesSyncPerformer<>(remoteEntries, localData.<T>execute())
                .setCallback(new RemoteMasterSyncCallback())
                .perform();
    }
}
