package com.timappweb.timapp.sync.performers;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.sync.exceptions.HttpResponseSyncException;
import com.timappweb.timapp.utils.Util;

import java.io.IOException;
import java.util.List;

/**
 * Created by Stephane on 14/08/2016.
 *
 * Perform a full table synchronisation.
 * If data cannot be load in one request, it will repeat the request until
 * all rows are downloaded or MAX_SYNC times is reached.
 *
 */
public class FullTableSyncPerformer<T extends SyncBaseModel> implements SyncPerformer {

    private static final String TAG = "FullTableSyncPerformer";
    private static final int MAX_SYNC = 15;

    private SyncAdapterOption params;
    private Class<T> clazz;
    private Callback<T> callback;
    private DataSyncAdapter.RemoteLoader<ResponseSyncWrapper, T> remoteLoader;

    public FullTableSyncPerformer(Class<T> clazz) {
        this.clazz = clazz;
        this.params = new SyncAdapterOption();
    }

    public void sync(List<T> remoteEntries) {
        try {
            ActiveAndroid.beginTransaction();
            // Update and remove existing items. Loop over local entries
            for (T remoteEntry: remoteEntries){
                if (callback.beforeSave(remoteEntry)) {
                    T entry = (T) remoteEntry.deepSave();
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
    public void perform() throws IOException, HttpResponseSyncException {
        int i = 0;
        long maxCreated = getMaxCreated();
        params.setLastUpdate(maxCreated);
        while (i < MAX_SYNC){
            ResponseSyncWrapper data = this.remoteLoader.load(params.toMap());
            this.sync(data.items);
            if (data.up_to_date){
                Log.d(TAG, "Sync is fully done");
                return;
            }
            params.setLastUpdate(data.last_update);
            i++;
        }
        Log.e(TAG, "Max sync number reached for " +clazz.getCanonicalName()+ ": " + MAX_SYNC);
    }

    public long getMaxCreated() {
        return SyncBaseModel.getMaxCreated(clazz, null);
    }

    public SyncAdapterOption getSyncParams() {
        return params;
    }

    public FullTableSyncPerformer<T> setCallback(Callback<T> callback) {
        this.callback = callback;
        return this;
    }

    public FullTableSyncPerformer<T> setRemoteLoader(DataSyncAdapter.RemoteLoader<ResponseSyncWrapper, T> remoteLoader) {
        this.remoteLoader = remoteLoader;
        return this;
    }

    public interface Callback<T> {

        boolean beforeSave(T entry);

        void afterSave(T entry);

    }


}
