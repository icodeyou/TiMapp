package com.timappweb.timapp.rest.callbacks;

import android.util.Log;

import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.sync.performers.RemoteMasterSyncPerformer;

import java.util.List;

import retrofit2.Response;

/**
 * Created by Stephane on 08/08/2016.
 */
public class RemoteMasterSyncCallback<T extends SyncBaseModel> extends HttpCallback<List<T>>{

    private static final String TAG = "RemoteMasterSyncCB";
    private final From localData;

    public RemoteMasterSyncCallback(Class<T> clazz, From from) {
        Log.i(TAG, "Performing model sync for " + clazz.getCanonicalName() + "...");
        this.localData = from;
    }

    @Override
    public void successful(List<T> remoteEntries) {
        new RemoteMasterSyncPerformer(remoteEntries, localData.<SyncBaseModel>execute(), null).perform();
    }
}
