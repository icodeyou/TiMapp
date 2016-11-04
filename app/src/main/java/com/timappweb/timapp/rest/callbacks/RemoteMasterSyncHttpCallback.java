package com.timappweb.timapp.rest.callbacks;

import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.From;
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
        try {
            new MultipleEntriesSyncPerformer<>(remoteEntries, localData.<T>queryList())
                    .setCallback(new RemoteMasterSyncCallback())
                    .perform();
        } catch (Exception e) {
            Log.e(TAG, "Error while sync: " + e.getMessage()); // TODO
        }
    }
}
