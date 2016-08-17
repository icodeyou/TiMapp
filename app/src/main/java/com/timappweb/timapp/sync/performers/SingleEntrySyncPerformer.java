package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;
import android.util.Log;

import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.rest.callbacks.HttpCallbackGroup;
import com.timappweb.timapp.rest.managers.HttpCallManager;

import java.io.IOException;

import retrofit2.Call;
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
    public void perform() {
        try {
            if (response.isSuccessful()){
                SyncBaseModel model = response.body();
                if (model != null){
                    Log.d(TAG, "Server returned a object. Synchronizing with local entry.");
                    model.deepSave();
                }
                else{
                    Log.e(TAG, "Server returned a null response when performing a entry sync");
                }
            }
            else if (response.code() == 404){
                // Remove in model
                Log.i(TAG, "Requested remote entry does not exists anymore: ");
                SyncBaseModel.deleteByRemoteId(classType, key);
            }
            else {
                // TODO handle this case globally ???
                Log.e(TAG, "Cannot synchronise, api response invalid: " + response.code());
            }
        } catch (CannotSaveModelException e) {
            e.printStackTrace();
        }
    }


}
