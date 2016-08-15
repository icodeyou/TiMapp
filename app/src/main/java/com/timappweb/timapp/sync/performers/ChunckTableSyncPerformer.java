package com.timappweb.timapp.sync.performers;

import android.database.Cursor;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.query.Select;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.rest.SyncStatus;
import com.timappweb.timapp.rest.io.request.SyncParams;
import com.timappweb.timapp.utils.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Stephane on 14/08/2016.
 */
public class ChunckTableSyncPerformer<T extends SyncBaseModel> implements SyncPerformer {

    private static final String TAG = "FullTableSyncPerformer";
    private static final int MAX_SYNC = 15;

    // ---------------------------------------------------------------------------------------------

    private SyncParams      params;
    private Class<T>        clazz;
    private Callback<T>     callback;
    private RemoteLoader<T> remoteLoader;

    // ---------------------------------------------------------------------------------------------

    public ChunckTableSyncPerformer(Class<T> clazz, Callback<T> callback, RemoteLoader<T> remoteLoader) {
        this.clazz = clazz;
        this.callback = callback;
        this.remoteLoader = remoteLoader;
        this.params = new SyncParams();
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
            while (i < MAX_SYNC){
                int maxCreated = getMaxCreated();
                params.setMaxCreated(maxCreated);
                List<T> data = this.remoteLoader.load(params.toMap());
                this.sync(data);
                if (data.size() < params.getLimit()){
                    Log.d(TAG, "Sync is fully done");
                    return;
                }
                i++;
            }
            Log.e(TAG, "Max sync number reached for " +clazz.getCanonicalName()+ ": " + MAX_SYNC);
        } catch (IOException e) {
            Log.e(TAG, "Exception while sync: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getMaxCreated() {
        Cursor cursor = ActiveAndroid.getDatabase().rawQuery("SELECT MAX(Created) FROM " + Cache.getTableInfo(clazz).getTableName() + ";", null);
        if (cursor.moveToNext()) {
            return cursor.getInt(0);
        }
        else{
            return 0;
        }
    }

    public SyncParams getSyncParams() {
        return params;
    }

    public interface Callback<T> {

        boolean beforeSave(T entry);

        void afterSave(T entry);

    }

    public interface RemoteLoader<T>{

        List<T> load(HashMap<String, String> options) throws IOException;
    }


    // ---------------------------------------------------------------------------------------------


    public static SyncStatus syncDone(String table, long minCreated, long maxCreated, long lastUpdate, int qty, int limit) {
        // Check if the new chunk if just after the previous on

        // If we reach the limit, we check if we have all data until the last chunk
        if (qty == limit){
            SyncStatus previousSection = new Select().from(SyncStatus.class)
                    .where("Table = ? AND MaxCreated <= ", table, minCreated)
                    .orderBy("MaxCreated ASC")
                    .limit(1)
                    .executeSingle();
            if (previousSection.getMaxCreated() < minCreated){
                // We need to create
                SyncStatus.create(table, previousSection.getMaxCreated(), minCreated, SyncStatus.SyncStatusType.SKIPPED);
            }
        }
        return null;
    }

    public static SyncStatus loadOlder(String table, long oldestLoaded) {
        new Select()
                .from(SyncStatus.class)
                .where("MaxCreated < ?", oldestLoaded)
                .executeSingle();
        return null;
    }
}
