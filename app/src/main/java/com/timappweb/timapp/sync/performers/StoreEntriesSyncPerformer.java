package com.timappweb.timapp.sync.performers;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.timappweb.timapp.data.models.MyModel;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;
import com.timappweb.timapp.sync.exceptions.HttpResponseSyncException;
import com.timappweb.timapp.utils.Util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by stephane on 5/5/2016.
 *
 * Performs match when data from server are the actual master data.
 *  - All local data will be overwritten.
 *  - Missing corresponding data will be removed
 */
public class StoreEntriesSyncPerformer<EntityType extends MyModel, RemoteCallType> implements SyncPerformer {

    private static final String TAG = "RemoteMasterSyncPerf";

    private DataSyncAdapter.RemoteLoader   remoteLoader;
    protected Callback                              callback;
    private SyncAdapterOption               syncOptions;

    public StoreEntriesSyncPerformer() {

    }

    public StoreEntriesSyncPerformer<EntityType, RemoteCallType> setRemoteLoader(DataSyncAdapter.RemoteLoader<RemoteCallType, EntityType> loader){
        this.remoteLoader = loader;
        return this;
    }

    public StoreEntriesSyncPerformer setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    /**
     *
    */
    @Override
    public void perform() throws IOException, CannotSyncException {
        Util.appAssert(callback != null, TAG, "Callback MUST not be null. Please fix this.");
        Util.appAssert(remoteLoader != null, TAG, "remoteLoader MUST not be null. Please fix this.");
        List<EntityType> remoteEntries = remoteLoader.items(syncOptions != null ? syncOptions.toMap() : new HashMap<String, String>());

        try
        {
            ActiveAndroid.beginTransaction();

            callback.before();

            // Update and remove existing items. Loop over local entries
            for (EntityType entry: remoteEntries){
                callback.save(entry);
            }

            callback.after();

            Log.i(TAG, "Merge solution ready. Applying updates");
            ActiveAndroid.setTransactionSuccessful();
        } catch (CannotSaveModelException e) {
            throw new CannotSyncException("Cannot save model: " + e.getMessage(), 0);
        } finally {
            Log.i(TAG, "Merge solution done");
            ActiveAndroid.endTransaction();
        }
    }

    public StoreEntriesSyncPerformer<EntityType, RemoteCallType> setSyncOptions(SyncAdapterOption syncOptions) {
        this.syncOptions = syncOptions;
        return this;
    }

    public interface Callback<EntityType>{

        void save(EntityType remoteModel) throws CannotSaveModelException;

        void before();

        void after();
    }

}
