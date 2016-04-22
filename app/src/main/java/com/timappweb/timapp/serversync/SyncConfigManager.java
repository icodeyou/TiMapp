package com.timappweb.timapp.serversync;

import android.content.Context;
import android.util.Log;

import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.utils.Util;

import retrofit2.Response;

/**
 * Created by stephane on 4/21/2016.
 */
public class SyncConfigManager<DataType> {

    public static final long MIN_REMOTE_SYNC_DELAY = 60 * 60 * 1000;
    private static final String TAG = "SyncConfigManager";

    // ---------------------------------------------------------------------------------------------
    protected SyncConfig dataWrapper;
    protected long minRemoteSyncDelay;

    // -------------------
    protected int configId;
    protected LocalPersistenceManager local;
    protected RemotePersistenceManager remote;
    protected OnConfigChangeListener listener;

    public DataType getData(){
        return (DataType) this.dataWrapper.data;
    }

    public SyncConfigManager(int configId) {
        this.dataWrapper = new SyncConfig();
        this.minRemoteSyncDelay = MIN_REMOTE_SYNC_DELAY;
        this.configId = configId;
        this.listener = null;
    }

    public SyncConfigManager(int configId, RemotePersistenceManager remote, LocalPersistenceManager local) {
        Log.d(TAG, "Initialize config id " + configId);
        this.dataWrapper = null;
        this.minRemoteSyncDelay = MIN_REMOTE_SYNC_DELAY;
        this.configId = configId;
        this.listener = null;
        this.setLocalManager(local);
        this.setRemoteManager(remote);
    }

    public void sync() throws RemotePersistenceManager.CannotLoadException {
        Log.d(TAG, this.configId + "] Check last sync: " + (this.dataWrapper != null ? this.dataWrapper.lastSync : "NONE")
                + " Current version: " + (this.dataWrapper != null ? this.dataWrapper : "NONE"));
        if (    this.dataWrapper == null
                || this.dataWrapper.version == 0
                || (System.currentTimeMillis() - this.dataWrapper.lastSync) > this.minRemoteSyncDelay){
            this.remoteSync();
        }
        else{
            Log.d(TAG, this.configId + "] config up to date! ");
        }
    }

    public void remoteSync() throws RemotePersistenceManager.CannotLoadException {
        int version = dataWrapper != null ? dataWrapper.version : 0;
        SyncConfig config = this.remote.load(version);
        if (config.data != null){
            this.dataWrapper = config;
            this.dataWrapper.lastSync = System.currentTimeMillis();;
            Log.d(TAG, this.configId + "] Updating configuration, version is now " + this.dataWrapper.version);
            this.saveLocal();
            if (this.listener != null){
                this.listener.onSyncChanged(configId, dataWrapper);
            }
        }
        else{
            Log.d(TAG, this.configId + "] version up to date, no data sent back");
        }

    }

    public void saveLocal(){
        if (this.local != null){
            this.local.write(this.dataWrapper);
        }
    }

    public SyncConfigManager<DataType> setLocalManager(LocalPersistenceManager local){
        this.local = local;
        this.localSync();
        return this;
    }

    private void localSync() {
        this.dataWrapper = this.local.load();
        Log.d(TAG, this.configId + "] Loading configuration from local: " + (this.dataWrapper != null ? "version " + this.dataWrapper.version : "NONE"));
    }

    public SyncConfigManager<DataType> setRemoteManager(RemotePersistenceManager remote){
        this.remote = remote;
        return this;
    }
    public SyncConfigManager<DataType> setOnConfigChangeListener(OnConfigChangeListener listener){
        this.listener = listener;
        return this;
    }

    @Override
    public String toString() {
        return "SyncConfigManager{" +
                "version=" + (dataWrapper != null ? dataWrapper.version : "NULL") +
                ", minRemoteSyncDelay=" + minRemoteSyncDelay +
                '}';
    }

    public void clear() {
        Log.d(TAG, this.configId + "] clearing local data");
        this.local.clear();
    }

}
