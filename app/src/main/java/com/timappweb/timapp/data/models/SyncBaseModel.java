package com.timappweb.timapp.data.models;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.property.BaseProperty;
import com.raizlabs.android.dbflow.sql.language.property.Property;
import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.data.tables.BaseTable;
import com.timappweb.timapp.utils.Util;

import java.lang.reflect.Field;

/**
 * Created by stephane on 4/23/2016.
 *
 * Class representing data that need synchronisation from the remote server.
 */
public abstract class SyncBaseModel extends MyModel implements SyncHistory.HistoryItemInterface {

    private static final String TAG = "SyncBaseModel";
    public static int SYNC_INTERVAL = 3600 * 1000;

    // =============================================================================================

    @PrimaryKey(autoincrement = false)
    @Column(name = "id")
    @Expose(serialize = true, deserialize = true)
    @SerializedName("id")
    public Long id = null;

    @Column
    protected long _last_sync;

    @Column//, notNull = true)
    @Expose(serialize = true, deserialize = true)
    public long created;

    // =============================================================================================

    /**
     * Return true if all specified fields in 'this' are the same as those in 'model'
     * @param model
     * @return
     */
    public abstract boolean isSync(SyncBaseModel model);

    public long getRemoteId(){
        return this.id != null ? this.id : 0L;
    }

    /**
     * Return true if last merge date does not exceed merge interval
     * @return
     */
    public boolean isUpToDate() {
        return (System.currentTimeMillis() - this._last_sync) < SYNC_INTERVAL;
    }

    public long getLastSync(){
        return this._last_sync;
    }


    /**
     * Get a entry in the db. If no entry, request an immediate merge with the server
     * @param classType The entry class type
     * @param context   The context
     * @param key       The remote key id
     * @param syncType  The merge type to call
     * @return If there is a local version of the entry, retu
     */
    public static SyncBaseModel getEntry(Class<? extends SyncBaseModel> classType, BaseProperty property, Context context, long key, int syncType) {
        SyncBaseModel model = BaseTable.loadByRemoteId(classType, property, key);
        if (model != null){
            if (model.isUpToDate()){
                Log.i(TAG, "Entry exists in local db and is up to date: " + model);
                return model;
            }
            else{
                Log.i(TAG, "Entry exists in local db but it's outdated: " + model);
            }
        }
        BaseTable.syncEntry(classType, context, key, syncType);
        return null;
    }

    /**
     * Sync and persist modification to database
     * @param model
     */
    public void merge(SyncBaseModel model) throws CannotSaveModelException {
        this.merge(model, true);
    }

    /**
     * Synchronise all fields that have the annotation @Expose(deserialize == true) from the given model
     * Last merge time is updated with the current timestamp
     * @param model
     * @param persist true if we save modification in db. If yes,
     */
    public void merge(SyncBaseModel model, boolean persist) throws CannotSaveModelException {
        // For each deserializable fields, we update the value if it is not null
        Class<? extends SyncBaseModel> clazz = this.getClass();
        for (Field field: clazz.getFields()){
            if (field.isAnnotationPresent(Expose.class)){
                Expose annotation = field.getAnnotation(Expose.class);
                if (annotation.deserialize()){
                    try {
                        Object newValue =  model.getClass().getField(field.getName()).get(model);
                        // TODO [Sync,Stef] check if remoteField is set or not. Currently, in the case we want to set to null a remoteField
                        // (thanks to a json response from the server), it does not work
                        if (newValue != null){
                            Log.v(TAG, "Updating remoteField " + field.getName() + " with value " + newValue);
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
            //if (this.getId() == null){
                //this.save();
                //Log.e(TAG,"Cannot persist a non existing model: " + this);
                //return;
            //}
            this.deepSave();
        }
    }

    /**
     * Update the last merge time
     */
    public final void updateLastSyncTime(){
        this._last_sync = System.currentTimeMillis();
    }

    public boolean hasRemoteId() { return this.id != null;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        SyncBaseModel that = (SyncBaseModel) o;
        //Log.d(TAG, "UD="+id + " THAT.ID=" + that.id + " ==? " + id.equals(that.id));
        return id != null ? id.equals(that.id) : that.id == null;
        //return equals;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String hashHistoryKey() {
        if (!this.hasRemoteId()){
            Util.appStateError(TAG, "This model does not have a remote id. It cannot be stored in sync history");
            return "";
        }
        return String.valueOf(this.getRemoteId());
    }

    public void deepSaveSafeCall() {
        try {
            this.deepSave();
        } catch (CannotSaveModelException e) {
            Log.e(TAG, e.getMessage());
            if (BuildConfig.DEBUG){
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete association data
     * @param associationModel
     */
    @Override
    public void deleteAssociation(Class<? extends MyModel> associationModel, Property property){
        SQLite.delete(associationModel)
                .where(property.eq(this.id))
                .execute();
    }


    public abstract int getSyncType();
}
