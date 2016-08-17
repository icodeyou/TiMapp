package com.timappweb.timapp.sync.performers;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.rest.io.responses.TableSyncResult;
import com.timappweb.timapp.utils.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Stephane on 14/08/2016.
 */
public class FullTableSyncPerformer<T extends SyncBaseModel> implements SyncPerformer {

    private static final String TAG = "FullTableSyncPerformer";
    private static final int MAX_SYNC = 15;

    private SyncAdapterOption params;
    private Class<T> clazz;
    private Callback<T> callback;
    private RemoteLoader<T> remoteLoader;

    public FullTableSyncPerformer(Class<T> clazz, Callback<T> callback, RemoteLoader<T> remoteLoader) {
        this.clazz = clazz;
        this.callback = callback;
        this.remoteLoader = remoteLoader;
        this.params = new SyncAdapterOption();
    }

    public void sync(List<T> remoteEntries) {
        try {
            ActiveAndroid.beginTransaction();
            // Update and remove existing items. Loop over local entries
            for (T remoteEntry: remoteEntries){
                if (callback.beforeSave(remoteEntry)) {
                    T entry = (T) remoteEntry.mySave();
                    callback.afterSave(entry);
                }
            }
            Log.i(TAG, "Merge solution ready. Applying updates");
            ActiveAndroid.setTransactionSuccessful();
        }
        catch (Exception ex){
            Util.appStateError(TAG, ex.toString());
        }
        finally {
            Log.i(TAG, "Merge solution done");
            ActiveAndroid.endTransaction();
        }
    }

    @Override
    public void perform() {
        try {
            int i = 0;
            long maxCreated = getMaxCreated();
            params.setLastUpdate(maxCreated);
            while (i < MAX_SYNC){
                TableSyncResult data = this.remoteLoader.load(params.toMap());
                this.sync(data.items);
                if (data.up_to_date){
                    Log.d(TAG, "Sync is fully done");
                    return;
                }
                params.setLastUpdate(data.last_update);
                i++;
            }
            Log.e(TAG, "Max sync number reached for " +clazz.getCanonicalName()+ ": " + MAX_SYNC);
        } catch (IOException e) {
            Log.e(TAG, "Exception while sync: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public long getMaxCreated() {
        return SyncBaseModel.getMaxCreated(clazz, null);
    }

    public SyncAdapterOption getSyncParams() {
        return params;
    }

    public interface Callback<T> {

        boolean beforeSave(T entry);

        void afterSave(T entry);

    }

    public interface RemoteLoader<T>{

        TableSyncResult<T> load(HashMap<String, String> options) throws IOException;

    }

}
