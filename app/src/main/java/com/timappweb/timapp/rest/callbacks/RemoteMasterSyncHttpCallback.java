package com.timappweb.timapp.rest.callbacks;

import android.util.Log;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;
import com.timappweb.timapp.data.AppDatabase;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserTag;
import com.timappweb.timapp.data.models.UserTag_Table;
import com.timappweb.timapp.sync.callbacks.RemoteMasterSyncCallback;
import com.timappweb.timapp.sync.exceptions.HttpResponseSyncException;

import java.io.IOException;
import java.util.List;

/**
 * Created by Stephane on 08/08/2016.
 */
public class RemoteMasterSyncHttpCallback<T extends SyncBaseModel> extends HttpCallback<List<T>>{

    private static final String TAG = "RemoteMasterSyncCB";
    private final From deleteQuery;

    public RemoteMasterSyncHttpCallback(Class<T> clazz, From deleteQuery) {
        Log.i(TAG, "Performing model sync for " + clazz.getCanonicalName() + "...");
        this.deleteQuery = deleteQuery;
    }

    @Override
    public void successful(final List<T> remoteEntries) throws IOException, HttpResponseSyncException {
        FlowManager.getDatabase(AppDatabase.class).executeTransaction(new ITransaction() {
            @Override
            public void execute(DatabaseWrapper databaseWrapper) {
                RemoteMasterSyncHttpCallback.this.deleteQuery.execute();
                for (T entry: remoteEntries){
                    entry.mySaveSafeCall();
                }
                Log.i(TAG, "Replacing with " + remoteEntries.size() + " entrie(s)");
            }
        });
    }
}
