package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;
import android.util.Log;

import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;
import com.timappweb.timapp.sync.exceptions.HttpResponseSyncException;

import retrofit2.Response;

/**
 * Created by stephane on 5/5/2016.
 */
public class SingleEntrySyncPerformer implements SyncPerformer {

    private static final String TAG = "SingleEntrySyncPerf";
    private long key;
    private SyncResult syncResult;
    private Response<? extends SyncBaseModel> response;
    private Class<? extends SyncBaseModel> classType;

    public SingleEntrySyncPerformer(Class<? extends SyncBaseModel> classType, long key,  Response<? extends SyncBaseModel> response, SyncResult syncResult) {
        this.key = key;
        this.syncResult = syncResult;
        this.response = response;
        this.classType = classType;
    }

    /**
     * Performe a single entry merge with the remote server
     */
    @Override
    public void perform() throws CannotSyncException {
        try {
            if (!response.isSuccessful()){
                throw new HttpResponseSyncException(response, null);
            }
            SyncBaseModel model = response.body();
            if (model != null){
                Log.d(TAG, "Server returned a object. Synchronizing with local entry.");
                model.deepSave();
            }
            else{
                Log.e(TAG, "Server returned a null response when performing a entry sync");
            }
        } catch (CannotSaveModelException e) {
            throw new CannotSyncException("Internal error cannot save model: " + e.getMessage(), 0);
        }
    }


}
