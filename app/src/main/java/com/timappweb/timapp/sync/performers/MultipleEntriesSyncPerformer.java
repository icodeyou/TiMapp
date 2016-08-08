package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.timappweb.timapp.data.models.SyncBaseModel;

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
public abstract class MultipleEntriesSyncPerformer implements SyncPerformer {

    private static final String TAG = "RemoteMasterSyncPerf";

    private final List<? extends SyncBaseModel> remoteEntries;
    private final List<? extends SyncBaseModel> localEntries;
    protected final SyncResult syncResult;

    public MultipleEntriesSyncPerformer(List<? extends SyncBaseModel> remoteEntries,
                                        List<? extends SyncBaseModel> localEntries,
                                        SyncResult syncResult) {


        this.remoteEntries = remoteEntries;
        this.localEntries = localEntries;
        this.syncResult = syncResult;
    }

    /**
     *
    */
    @Override
    public void perform() {
        if (localEntries == null || localEntries.size() == 0){
            this.onRemoteOnly(remoteEntries);
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
                    this.onMatch(match, localModel);
                }
                // If the local entry does not match any remote entry we remove it
                else{
                    this.onLocalOnly(localModel);
                }
            }

            if (entryMap.values().size() > 0){
                this.onRemoteOnly(entryMap.values());
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

    public abstract void onMatch(SyncBaseModel remoteModel, SyncBaseModel localModel);

    public abstract void onRemoteOnly(Collection<? extends SyncBaseModel> values);

    public abstract void onLocalOnly(SyncBaseModel localModel);

}
