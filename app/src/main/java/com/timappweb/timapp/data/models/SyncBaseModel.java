package com.timappweb.timapp.data.models;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.annotations.ModelAssociation;
import com.timappweb.timapp.sync.DataSyncAdapter;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by stephane on 4/23/2016.
 *
 * Class representing data that need synchronisation from the remote server.
 */
public abstract class SyncBaseModel extends MyModel implements Serializable {

    private static final String TAG = "SyncBaseModel";
    public static int SYNC_INTERVAL = 3600 * 1000;

    // =============================================================================================

    @Column(name = "SyncId", index = true, unique = true)
    @Expose(serialize = true, deserialize = true)
    @SerializedName("id")
    public int remote_id = -1;

    @Column(name = "_lastSync")
    protected long _last_sync;

    // =============================================================================================

    /**
     * Return true if all specified fields in 'this' are the same as those in 'model'
     * @param model
     * @return
     */
    public abstract boolean isSync(SyncBaseModel model);

    public long getRemoteId(){
        return this.remote_id;
    }

    /**
     * Return true if last sync date does not exceed sync interval
     * @return
     */
    public boolean isUpToDate() {
        return (this._last_sync - System.currentTimeMillis()) < SYNC_INTERVAL;
    }

    /**
     *
     * @param classType
     * @param key
     * @return
     */
    public static From queryByRemoteId(Class<? extends SyncBaseModel> classType, int key) {
        return new Select().from(classType).where("SyncId = ?", key);
    }

    /**
     *
     * @param classType
     * @param key
     * @return
     */
    public static From deleteByRemoteId(Class<? extends SyncBaseModel> classType, int key) {
        return new Delete().from(classType).where("SyncId = ?", key);
    }

    /**
     * Get a entry in the db. If no entry, request an immediate sync with the server
     * @param classType The entry class type
     * @param context   The context
     * @param key       The remote key remote_id
     * @param syncType  The sync type to call
     * @return If there is a local version of the entry, retu
     */
    public static SyncBaseModel getEntry(Class<? extends SyncBaseModel> classType, Context context, int key, int syncType) {
        SyncBaseModel model = queryByRemoteId(classType, key).executeSingle();
        if (model != null){
            if (model.isUpToDate()){
                Log.i(TAG, "Entry exists in local db and is up to date: " + model);
                return model;
            }
            else{
                Log.i(TAG, "Entry exists in local db but it's outdated: " + model);
            }
        }
        getRemoteEntry(classType, context, key, syncType);
        return null;
    }
    /**
     * Request an immediate sync with the server to get data
     * @param classType The entry class type
     * @param context   The context
     * @param key       The remote key remote_id
     * @param syncType  The sync type to call
     * @return If there is a local version of the entry, retu
     */
    public static void getRemoteEntry(Class<? extends SyncBaseModel> classType, Context context, int key, int syncType) {
        Log.i(TAG, "Request sync for entry " + classType + " with id " + key);
        Bundle bundle = new Bundle();
        bundle.putInt(DataSyncAdapter.SYNC_TYPE_KEY, syncType);
        bundle.putInt(DataSyncAdapter.SYNC_ID_KEY, key);
        DataSyncAdapter.syncImmediately(context, context.getString(R.string.content_authority_data), bundle);
    }

    /**
     * Get remote entries for a specified model.
     * @param context
     * @param query
     * @param syncType
     * @return
     */
    public static <DataType extends SyncBaseModel> List<DataType> getEntries(Context context, From query, int syncType, long syncDelay){
        // If need sync
        if (SyncHistory.requireUpdate(syncType, syncDelay)){
            getRemoteEntries(context, syncType);
            return null;
        }
        else {
            Log.i(TAG, "Entries are already in local db for type: " + syncType);
            List<DataType> data = query.execute();
            return data;
        }
    }
    /**
     * Get remote entries for a specified model.
     * @param context
     * @param syncType
     * @return
     */
    public static void getRemoteEntries(Context context, int syncType){
        Bundle bundle = new Bundle();
        bundle.putInt(DataSyncAdapter.SYNC_TYPE_KEY, syncType);
        bundle.putLong(DataSyncAdapter.SYNC_LAST_TIME, SyncHistory.getLastSyncTime(syncType));
        DataSyncAdapter.syncImmediately(context, context.getString(R.string.content_authority_data), bundle);
    }

    /**
     * Saving the model from the remote server key (do not use ActiveAndroid primary key)
     * Il the record already exists, it is updated otherwise inserted
     */
    public void saveWithRemoteKey(){
        SyncBaseModel model = this.queryByRemoteId().executeSingle();
        if (model == null){
            long id = this.deepSave();
            Log.v(TAG, "Creating new entry " + this.getClass().getCanonicalName() + " with id " + id);
        }
        else{
            model.sync(this);
        }
    }

    /**
     * Sync and persist modification to database
     * @param model
     */
    public void sync(SyncBaseModel model){
        this.sync(model, true);
    }

    /**
     * Synchronise all fields that have the annotation @Expose(deserialize == true) from the given model
     * Last sync time is updated with the current timestamp
     * @param model
     * @param persist true if we save modification in db. If yes,
     */
    public void sync(SyncBaseModel model, boolean persist){
        // For each deserialisable fields, we update the value if it is not null
        Class<? extends SyncBaseModel> clazz = this.getClass();
        for (Field field: clazz.getFields()){
            if (field.isAnnotationPresent(Expose.class)){
                Expose annotation = field.getAnnotation(Expose.class);
                if (annotation.deserialize()){
                    try {
                        Object newValue =  model.getClass().getField(field.getName()).get(model);
                        // TODO [Sync,Stef] check if field is set of not. Currently, in the case we want to set to null a field
                        // (thanks to a json response from the server), it does not work
                        if (newValue != null){
                            Log.v(TAG, "Updating field " + field.getName() + " with value " + newValue);
                            field.set(this, newValue);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        this.updateLastSyncTime();

        if (persist){
            if (this.getId() == null){
                Log.e(TAG,"Cannot persist a non existing model: " + this);
                return;
            }
            this.deepSave();
        }
    }

    /**
     * Update the last sync time
     */
    public final void updateLastSyncTime(){
        this._last_sync = System.currentTimeMillis();
    }

    /**
     * Create a query for an entry thanks to the remote id
     *
     * @return
     */
    private From queryByRemoteId() {
        return queryByRemoteId(this.getClass(), (int) this.getRemoteId());
    }

    /**
     * Load an entry thanks to the remote id
     *
     * @param clazz
     * @param id
     * @return
     */
    public static <T extends SyncBaseModel> T loadByRemoteId(Class<T> clazz, int id) {
        return queryByRemoteId(clazz, id).executeSingle();
    }

}
