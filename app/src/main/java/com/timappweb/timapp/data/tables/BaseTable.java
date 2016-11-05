package com.timappweb.timapp.data.tables;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.sql.language.property.BaseProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.MyModel;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.SyncHistory;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.sync.data.DataSyncAdapter;

import java.util.List;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

/**
 * Created by Stephane on 03/11/2016.
 */

public class BaseTable {

    private static final String TAG = "BaseTable";

    public static void startSync(Context context, SyncAdapterOption params) {
        DataSyncAdapter.syncImmediately(context, context.getString(R.string.content_authority_data), params.toBundle());
    }

    /**
     * Request an immediate merge with the server to get data
     * @param classType The entry class type
     * @param context   The context
     * @param key       The remote key id
     * @param syncType  The merge type to call
     * @return If there is a local version of the entry, retu
     */
    public static void syncEntry(Class<? extends SyncBaseModel> classType, Context context, long key, int syncType) {
        Log.i(TAG, "Request sync for entry " + classType + " with id " + key);
        Bundle bundle = new Bundle();
        bundle.putInt(DataSyncAdapter.SYNC_TYPE_KEY, syncType);
        bundle.putLong(DataSyncAdapter.SYNC_ID_KEY, key);
        DataSyncAdapter.syncImmediately(context, context.getString(R.string.content_authority_data), bundle);
    }

    public static void requestSync(Context context, int syncType, long id) {
        Log.i(TAG, "Request sync update with [type=" + syncType + ";id=" + id);
        Bundle bundle = new Bundle();
        bundle.putInt(DataSyncAdapter.SYNC_TYPE_KEY, syncType);
        bundle.putLong(DataSyncAdapter.SYNC_ID_KEY, id);
        DataSyncAdapter.syncImmediately(context, context.getString(R.string.content_authority_data), bundle);
    }

    public static void requestSync(Context context, SyncBaseModel model) {
        BaseTable.requestSync(context, model.getSyncType(), model.getRemoteId());
    }


    /**
     * Load an entry thanks to the remote id
     *
     * @param clazz
     * @param id
     * @return
     */
    public static <T extends SyncBaseModel> T loadByRemoteId(Class<T> clazz, BaseProperty property, long id) {
        return SQLite.select()
                .from(clazz)
                .where(column(property.getNameAlias()).eq(id))
                .querySingle();
    }

    public static Where<? extends SyncBaseModel> queryByRemoteId(Class<? extends SyncBaseModel> clazz, BaseProperty property, long id) {
        return SQLite.select()
                .from(clazz)
                .where(column(property.getNameAlias()).eq(id))
                ;
    }



    /**
     * Get entries for a specified model. If entries exists locally, take it otherwise request a sync update
     * @param context
     * @param query
     * @return
     */
    public static <DataType extends SyncBaseModel> List<DataType>
            getEntries(Context context, SyncAdapterOption options, From query, long syncDelay){
        // If need merge
        if (SyncHistory.requireUpdate(options.getSyncType(), syncDelay)){
            Log.i(TAG, "Requesting remote entries for type: " + options);
            getRemoteEntries(context, options);
            return null;
        }
        else {
            Log.i(TAG, "Entries are already in local db for type: " + options);
            List<DataType> data = query.queryList();
            return data;
        }
    }
    /**
     * Get remote entries for a specified model.
     * @param context
     * @param syncOption
     * @return
     */
    public static void getRemoteEntries(Context context, SyncAdapterOption syncOption){
        syncOption.setLastSyncTime();
        DataSyncAdapter.syncImmediately(context, context.getString(R.string.content_authority_data), syncOption.getBundle());
    }

}
