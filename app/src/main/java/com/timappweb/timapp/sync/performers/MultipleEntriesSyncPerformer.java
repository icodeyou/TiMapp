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
 * Performs match when data from server the actual master data.
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
     * Read XML from an input stream, storing it into the content provider.
     *
     * <p>This is where incoming data is persisted, committing the results of a sync. In order to
     * minimize (expensive) disk operations, we compare incoming data with what's already in our
     * database, and compute a merge. Only changes (insert/update/delete) will result in a database
     * write.
     *
     * <p>As an additional optimization, we use a batch operation to perform all database writes at
     * once.
     *
     * <p>Merge strategy:
     * 1. Get cursor to all items in feed<br/>
     * 2. For each item, check if it's in the incoming data.<br/>
     *    a. YES: Remove from "incoming" list. Check if data has mutated, if so, perform
     *            database UPDATE.<br/>
     *    b. NO: Schedule DELETE from database.<br/>
     * (At this point, incoming database only contains missing items.)<br/>
     * 3. For any items remaining in incoming list, ADD to database.
    */
    @Override
    public void perform() {
        // Build hash table of remote entries
        HashMap<Long, SyncBaseModel> entryMap = new HashMap();
        for (SyncBaseModel m : remoteEntries) {
            entryMap.put(m.getRemoteId(), m);
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
        //        false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }

    public abstract void onMatch(SyncBaseModel remoteModel, SyncBaseModel localModel);

    public abstract void onRemoteOnly(Collection<? extends SyncBaseModel> values);

    public abstract void onLocalOnly(SyncBaseModel localModel);

}
