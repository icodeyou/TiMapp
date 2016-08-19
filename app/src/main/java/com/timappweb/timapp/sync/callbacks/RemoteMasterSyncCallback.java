package com.timappweb.timapp.sync.callbacks;

import android.content.SyncResult;
import android.util.Log;

import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;

import java.util.Collection;
import java.util.List;

import retrofit2.Response;

/**
 * Created by stephane on 5/5/2016.
 *
 * Performs match when data from server the actual master data.
 *  - All local data will be overwritten.
 *  - Missing corresponding data will be removed
 */
public class RemoteMasterSyncCallback<EntitiyType extends SyncBaseModel> implements MultipleEntriesSyncPerformer.Callback<EntitiyType> {

    private static final String TAG = "RemoteMasterSyncPerf";


    public void onMatch(EntitiyType remoteModel, EntitiyType localModel) {
        try {
            if (!remoteModel.isSync(localModel)){
                    localModel.merge(remoteModel);
                Log.i(TAG, "Updating: " + localModel.toString());
            }
            else{
                Log.i(TAG, "No action: " + localModel.toString());
            }
        } catch (CannotSaveModelException e) {
            //throw CannotSyncException("Cannot save model: " + e.getMessage());
        }
    }

    public void onRemoteOnly(Collection<EntitiyType> values){
        // Add new items
        for (EntitiyType m : values) {
            try {
                Log.i(TAG, "Scheduling insert: " + m.toString());
                m.deepSave();
            } catch (CannotSaveModelException e) {
               // e.printStackTrace();
            }
        }
    }

    public void onLocalOnly(EntitiyType localModel) {
        Log.i(TAG, "Deleting: " + localModel.toString());
        localModel.delete();
    }

}
