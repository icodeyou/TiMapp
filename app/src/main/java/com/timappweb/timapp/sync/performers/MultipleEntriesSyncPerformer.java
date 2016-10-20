package com.timappweb.timapp.sync.performers;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
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
public class MultipleEntriesSyncPerformer<EntityType extends SyncBaseModel, RemoteCallType> implements SyncPerformer {

    private static final String TAG = "RemoteMasterSyncPerf";

    protected List<EntityType>     remoteEntries;
    protected List<EntityType>     localEntries;
    private DataSyncAdapter.RemoteLoader   remoteLoader;
    protected Callback                              callback;
    private SyncAdapterOption               syncOptions;

    public MultipleEntriesSyncPerformer(List<EntityType> remoteEntries,
                                        List<EntityType> localEntries) {
        this.remoteEntries = remoteEntries;
        this.localEntries = localEntries;
    }

    public MultipleEntriesSyncPerformer() {

    }

    public MultipleEntriesSyncPerformer<EntityType, RemoteCallType> setRemoteLoader(DataSyncAdapter.RemoteLoader<RemoteCallType, EntityType> loader){
        this.remoteLoader = loader;
        return this;
    }

    public MultipleEntriesSyncPerformer setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    /**
     *
    */
    @Override
    public void perform() throws IOException, HttpResponseSyncException {
        if (remoteLoader != null){
            this.remoteEntries = remoteLoader.items(syncOptions != null ? syncOptions.toMap() : new HashMap<String, String>());
        }

        if (callback == null){
            Util.appStateError(TAG, "Callback MUST not be null. Please fix this.");
            return;
        }

        if (localEntries == null || localEntries.size() == 0){
            callback.onRemoteOnly(remoteEntries);
            return;
        }
        if (remoteEntries == null || remoteEntries.size() == 0){
            for (SyncBaseModel localEntry : localEntries){
                callback.onLocalOnly(localEntry);
            }
            return;
        }
        // Build hash table of remote entries
        HashMap<Long, SyncBaseModel> entryMap = new HashMap();
        for (SyncBaseModel m : remoteEntries) {
            if (m.hasRemoteId()) entryMap.put(m.getRemoteId(), m);
        }

        Log.i(TAG, "Found " + localEntries.size() + " local entries. Computing merge solution...");
        try
        {
            ActiveAndroid.beginTransaction();

            // Update and remove existing items. Loop over local entries
            for (SyncBaseModel localModel: localEntries){
                SyncBaseModel match = entryMap.get(localModel.getRemoteId());
                // If the local entry match a remote entry
                if (match != null) {
                    entryMap.remove(localModel.getRemoteId());
                    callback.onMatch(match, localModel);
                }
                // If the local entry does not match any remote entry we remove it
                else{
                    callback.onLocalOnly(localModel);
                }
            }

            if (entryMap.values().size() > 0){
                callback.onRemoteOnly(entryMap.values());
            }

            Log.i(TAG, "Merge solution ready. Applying updates");
            ActiveAndroid.setTransactionSuccessful();
        }
        finally {
            Log.i(TAG, "Merge solution done");
            ActiveAndroid.endTransaction();
        }

        // mContentResolver.notifyChange(
        //        FeedContract.Entry.CONTENT_URI, // URI where data was modified
        //        null,                           // No local observer
        //        false);                         // IMPORTANT: Do not merge to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }

    public MultipleEntriesSyncPerformer<EntityType, RemoteCallType> setSyncOptions(SyncAdapterOption syncOptions) {
        this.syncOptions = syncOptions;
        return this;
    }

    public MultipleEntriesSyncPerformer setLocalEntries(List<EntityType> localEntries){
        this.localEntries = localEntries;
        return this;
    }

    public DataSyncAdapter.RemoteLoader getRemoteLoader() {
        return remoteLoader;
    }


    public MultipleEntriesSyncPerformer<EntityType, RemoteCallType> setRemoteEntries(List<EntityType> entries) {
        this.remoteEntries = entries;
        return this;
    }

    public interface Callback<EntityType>{
        void onMatch(EntityType remoteModel, EntityType localModel);

        void onRemoteOnly(Collection<EntityType> values);

        void onLocalOnly(EntityType localModel);

    }

}
