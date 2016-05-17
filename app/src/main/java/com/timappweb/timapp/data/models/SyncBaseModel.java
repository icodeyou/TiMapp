package com.timappweb.timapp.data.models;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.annotation.Column;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.R;
import com.timappweb.timapp.listeners.BinaryActionListener;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.sync.performers.SyncAdapterOption;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import retrofit2.Call;

/**
 * Created by stephane on 4/23/2016.
 *
 * Class representing data that need synchronisation from the remote server.
 */
public abstract class SyncBaseModel extends MyModel implements Serializable {

    private static final String TAG = "SyncBaseModel";
    public static int SYNC_INTERVAL = 3600 * 1000;

    // =============================================================================================

    @Column(name = "SyncId", index = true, unique = true, notNull = true) // onUniqueConflict = Column.ConflictAction.IGNORE
    @Expose(serialize = true, deserialize = true)
    @SerializedName("id")
    public Integer remote_id = null;

    @Column(name = "_lastSync", notNull = false)
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

    public void setRemoteId(int remoteId) {
        this.remote_id = remoteId;
    }
    /**
     *
     * Saving the model from the remote server key if there is no ActiveAndroid primary key.
     * Il the record already exists, it is updated otherwise inserted
     *
     * @return
     */
    public <T extends MyModel> T deepSave(){
        /*
        if (!this.hasLocalId()){
            SyncBaseModel model = this.queryByRemoteId().executeSingle();
            if (model != null){
                model.merge(this);
                Log.d(TAG, "Updating existing remote model: " + model);
                return (T) model;
            }
        }*/
        return super.deepSave();
    }

    @Override
    public MyModel mySave() {
        if (!this.hasLocalId() && this.hasRemoteId()){
            SyncBaseModel model = this.queryByRemoteId().executeSingle();
            if (model != null){
                model.merge(this);
                Log.d(TAG, "Updating existing remote model: " + model);
                return model;
            }
        }
        return super.mySave();
    }

    /**
     * Return true if last merge date does not exceed merge interval
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
    public static From queryByRemoteId(Class<? extends SyncBaseModel> classType, long key) {
        return new Select().from(classType).where("SyncId = ?", key);
    }

    /**
     *
     * @param classType
     * @param key
     * @return
     */
    public static From deleteByRemoteId(Class<? extends SyncBaseModel> classType, long key) {
        return new Delete().from(classType).where("SyncId = ?", key);
    }

    /**
     * Get a entry in the db. If no entry, request an immediate merge with the server
     * @param classType The entry class type
     * @param context   The context
     * @param key       The remote key remote_id
     * @param syncType  The merge type to call
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


    public void saveRemoteEntry(Context context, Call call){
        saveRemoteEntry(context, call, null);
    }

    /**
     * Saving remote entry
     * @param context
     * @param call
     * @param binaryActionListener
     */
    public void saveRemoteEntry(Context context, Call call, final BinaryActionListener binaryActionListener){
        // try {
            call.enqueue(new RestFeedbackCallback(context) {
                @Override
                public void onActionSuccess(RestFeedback feedback) {
                    int id = feedback.getIntData("id");
                    if (id != -1) {
                        setRemoteId(id);
                    }
                    SyncBaseModel.this.mySave();
                    if (binaryActionListener != null) binaryActionListener.onSuccess();
                }

                @Override
                public void onActionFail(RestFeedback feedback) {
                    Log.e(TAG, "Cannot save entry on remote");
                    if (feedback.message != null) {
                        Toast.makeText(context, feedback.message, Toast.LENGTH_LONG).show();
                    }
                    if (binaryActionListener != null) binaryActionListener.onFailure();
                }

                @Override
                public void onFinish() {
                    if (binaryActionListener != null) binaryActionListener.onFinish();
                }
            });
            //call.execute();
            //} catch (IOException e) {
            //    Log.e(TAG, "Error " + e.getMessage());
            //    e.printStackTrace();
            //    if (binaryActionListener != null){
            //       binaryActionListener.onFailure();
            //       binaryActionListener.onFinish();
            //    }
            //}
    }

    /**
     * Request an immediate merge with the server to get data
     * @param classType The entry class type
     * @param context   The context
     * @param key       The remote key remote_id
     * @param syncType  The merge type to call
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
     * Get entries for a specified model. If entries exists locally, take it otherwise request a sync update
     * @param context
     * @param query
     * @return
     */
    public static <DataType extends SyncBaseModel> List<DataType> getEntries(Context context, SyncAdapterOption options, From query, long syncDelay){
        // If need merge
        if (SyncHistory.requireUpdate(options.getSyncType(), syncDelay)){
            getRemoteEntries(context, options);
            return null;
        }
        else {
            Log.i(TAG, "Entries are already in local db for type: " + options);
            List<DataType> data = query.execute();
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

    /**
     * Sync and persist modification to database
     * @param model
     */
    public void merge(SyncBaseModel model){
        this.merge(model, true);
    }

    /**
     * Synchronise all fields that have the annotation @Expose(deserialize == true) from the given model
     * Last merge time is updated with the current timestamp
     * @param model
     * @param persist true if we save modification in db. If yes,
     */
    public void merge(SyncBaseModel model, boolean persist){
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
     * Update the last merge time
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
    public static <T extends SyncBaseModel> T loadByRemoteId(Class<T> clazz, long id) {
        return queryByRemoteId(clazz, id).executeSingle();
    }

    public boolean hasRemoteId() { return this.remote_id != null;}


}
