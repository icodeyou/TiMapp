package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.data.models.UserQuota;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by stephane on 5/5/2016.
 *
 * Performs match when data from server are the actual master data.
 *  - All local data will be overwritten.
 *  - Missing corresponding data will be removed
 */
public class  MultipleEntriesSyncPerformer<T extends SyncBaseModel>implements SyncPerformer {

    private static final String TAG = "RemoteMasterSyncPerf";

    private final List<T>     remoteEntries;
    private final List<T>     localEntries;
    protected Callback                              callback;

    public MultipleEntriesSyncPerformer(List<T> remoteEntries,
                                        List<T> localEntries) {


        this.remoteEntries = remoteEntries;
        this.localEntries = localEntries;
    }

    public MultipleEntriesSyncPerformer(Call<PaginatedResponse<T>> call, List<T> localEntries, int syncType) throws IOException, CannotSyncException {
        Response<PaginatedResponse<T>> response = call.execute();
        if (!response.isSuccessful()){
            throw new CannotSyncException("Request not successfull", syncType);
        }
        this.remoteEntries = response.body().items;
        this.localEntries = localEntries;
    }

    public MultipleEntriesSyncPerformer(Call<List<T>> call, List<T> localEntries, int syncType, boolean b) throws CannotSyncException, IOException {
        Response<List<T>> response = call.execute();
        if (!response.isSuccessful()){
            throw new CannotSyncException("Request not successfull", syncType);
        }
        this.remoteEntries = response.body();
        this.localEntries = localEntries;
    }


    public MultipleEntriesSyncPerformer<T> setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    /**
     *
    */
    @Override
    public void perform() {

        if (localEntries == null || localEntries.size() == 0){
            callback.onRemoteOnly(remoteEntries);
            return;
        }
        // Build hash table of remote entries
        HashMap<Long, SyncBaseModel> entryMap = new HashMap();
        for (SyncBaseModel m : remoteEntries) {
            if (m.hasRemoteId()) entryMap.put(m.getRemoteId(), m);
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
                    callback.onMatch(match, localModel);
                }
                // If the local entry does not match any remote entry we remove it
                else{
                    callback.onLocalOnly(localModel);
                }
            }

            if (entryMap.values().size() > 0){
                callback.onRemoteOnly(entryMap.values());
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
        //        false);                         // IMPORTANT: Do not merge to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }


    public interface Callback{
        void onMatch(SyncBaseModel remoteModel, SyncBaseModel localModel);

        void onRemoteOnly(Collection<? extends SyncBaseModel> values);

        void onLocalOnly(SyncBaseModel localModel);

    }

}
