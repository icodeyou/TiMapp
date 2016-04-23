package com.timappweb.timapp.configsync;

import android.util.Log;

/**
 * Created by stephane on 4/21/2016.
 */
public class SyncConfigManager<DataType> {

    public static final long MIN_REMOTE_SYNC_DELAY = 60 * 60 * 1000;
    private static final String TAG = "SyncConfigManager";

    // ---------------------------------------------------------------------------------------------
    protected SyncConfig<DataType> dataWrapper;
    protected long minRemoteSyncDelay;

    // -------------------
    protected int configId;
    protected LocalPersistenceManager local;
    protected RemotePersistenceManager remote;
    protected OnConfigChangeListener listener;

    public DataType getData(){
        return this.dataWrapper.data;
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
            Log.v(TAG, this.configId + "] dataWrapper=" + this.dataWrapper);
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
        //this.loadLocalConf();
        return this;
    }

    private void loadLocalConf() {
        this.dataWrapper = this.local.load();
        if (this.dataWrapper != null && this.dataWrapper.data == null){
            Log.e(TAG, "Error there is no data for the version " + this.dataWrapper.version);
            this.dataWrapper = null;
        }
        Log.d(TAG, this.configId + "] Loading configuration from local: " + (this.dataWrapper != null ? "version " + this.dataWrapper.version : "NONE"));
        Log.v(TAG, this.configId + "] " + (this.dataWrapper != null ? "version " + this.dataWrapper : "NONE"));
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
        this.dataWrapper = null;
        this.local.clear();
    }

    public SyncConfig getDataWrapper() {
        return dataWrapper;
    }
}
