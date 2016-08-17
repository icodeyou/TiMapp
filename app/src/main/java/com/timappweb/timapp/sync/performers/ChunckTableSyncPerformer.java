package com.timappweb.timapp.sync.performers;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.rest.io.responses.TableSyncResult;
import com.timappweb.timapp.utils.Util;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Stephane on 14/08/2016.
 */
public class ChunckTableSyncPerformer<T extends SyncBaseModel> implements SyncPerformer {

    private static final String TAG = "ChunckTableSyncPerfor";

    // ---------------------------------------------------------------------------------------------

    private SyncAdapterOption      params;
    private Class<T>        clazz;
    private Callback<T>     callback;
    private RemoteLoader<T> remoteLoader;

    public TableSyncResult<T> getResult() {
        return result;
    }

    private TableSyncResult<T> result;

    // ---------------------------------------------------------------------------------------------


    public ChunckTableSyncPerformer(Class<T> clazz) {
        this.clazz = clazz;
        this.params = new SyncAdapterOption();
    }

    public void setRemoteLoader(RemoteLoader<T> remoteLoader) {
        this.remoteLoader = remoteLoader;
    }

    public void setCallback(Callback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void perform() {
        try{
            this.result = this.remoteLoader.load(this.getSyncParams().toMap());
            ActiveAndroid.beginTransaction();
            // Update and remove existing items. Loop over local entries
            for (T remoteEntry: result.items){
                if (callback.beforeSave(remoteEntry, result)) {
                    T entry = (T) remoteEntry.mySave();
                    callback.afterSave(entry, result);
                }
            }
            Log.i(TAG, "Merge solution ready. Applying updates");
            ActiveAndroid.setTransactionSuccessful();
        }
        catch (IOException e) {
            Log.e(TAG, "Exception while sync: " + e.getMessage());
            e.printStackTrace();
        }
        catch (Exception ex){
            Util.appStateError(TAG, ex.toString());
        }
        finally {
            Log.i(TAG, "Merge solution done");
            ActiveAndroid.endTransaction();
        }

    }

    public SyncAdapterOption getSyncParams() {
        return params;
    }

    public void setSyncParam(SyncAdapterOption params) {
        this.params = params;
    }

    // ---------------------------------------------------------------------------------------------


    public interface Callback<T> {

        boolean beforeSave(T entry, TableSyncResult<T> result);

        void afterSave(T entry, TableSyncResult<T> result);

    }

    public interface RemoteLoader<T>{

        TableSyncResult<T> load(HashMap<String, String> options) throws IOException;
    }

}
