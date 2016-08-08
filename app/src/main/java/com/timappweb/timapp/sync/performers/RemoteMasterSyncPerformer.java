package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;
import android.util.Log;

import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.rest.model.PaginatedResponse;

import java.util.Collection;
import java.util.List;

/**
 * Created by stephane on 5/5/2016.
 *
 * Performs match when data from server the actual master data.
 *  - All local data will be overwritten.
 *  - Missing corresponding data will be removed
 */
public class RemoteMasterSyncPerformer extends MultipleEntriesSyncPerformer {

    private static final String TAG = "RemoteMasterSyncPerf";

    public RemoteMasterSyncPerformer(List<? extends SyncBaseModel> remoteEntries,
                                     List<? extends SyncBaseModel> localEntries,
                                     SyncResult syncResult) {
        super(remoteEntries, localEntries, syncResult);
    }

    public RemoteMasterSyncPerformer(PaginatedResponse<? extends SyncBaseModel> remoteEntries,
                                     List<? extends SyncBaseModel> localEntries,
                                     SyncResult syncResult) {
        super(remoteEntries.items, localEntries, syncResult);
    }



    public void onMatch(SyncBaseModel remoteModel, SyncBaseModel localModel) {
        if (!remoteModel.isSync(localModel)){
            localModel.merge(remoteModel);
            if (syncResult != null) syncResult.stats.numUpdates++;
            Log.i(TAG, "Updating: " + localModel.toString());
        }
        else{
            Log.i(TAG, "No action: " + localModel.toString());
        }
    }

    public void onRemoteOnly(Collection<? extends SyncBaseModel> values){
        // Add new items
        for (SyncBaseModel m : values) {
            Log.i(TAG, "Scheduling insert: " + m.toString());
            m.deepSave();
            if (syncResult != null) syncResult.stats.numInserts++;
        }
    }

    public void onLocalOnly(SyncBaseModel localModel) {
        Log.i(TAG, "Deleting: " + localModel.toString());
        localModel.delete();
        if (syncResult != null) syncResult.stats.numDeletes++;
    }
}
