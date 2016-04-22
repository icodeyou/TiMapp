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
    protected long lastSync;
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
        this.lastSync = 0;
        this.minRemoteSyncDelay = MIN_REMOTE_SYNC_DELAY;
        this.configId = configId;
        this.listener = null;
    }

    public SyncConfigManager(int configId, RemotePersistenceManager remote, LocalPersistenceManager local) {
        this.dataWrapper = new SyncConfig();
        this.lastSync = 0;
        this.minRemoteSyncDelay = MIN_REMOTE_SYNC_DELAY;
        this.configId = configId;
        this.listener = null;
        this.setLocalManager(local);
        this.setRemoteManager(remote);
    }

    public void sync() throws RemotePersistenceManager.CannotLoadException {
        Log.d(TAG, "Check last sync: " + (this.lastSync == 0 ? "NEVER" : this.lastSync - System.currentTimeMillis()));
        if (    this.dataWrapper == null
                || this.dataWrapper.version == 0
                || (this.lastSync - System.currentTimeMillis()) > this.minRemoteSyncDelay){
            this.remoteSync();
        }
    }

    public void remoteSync() throws RemotePersistenceManager.CannotLoadException {
        int version = dataWrapper != null ? dataWrapper.version : 0;
        SyncConfig config = this.remote.load(version);
        if (config.data != null){
            this.dataWrapper = config;
            Log.d(TAG, "Updating configuration, version is now " + this.dataWrapper.version);
            this.lastSync = System.currentTimeMillis();
            this.saveLocal();
            if (this.listener != null){
                this.listener.onSyncChanged(configId, dataWrapper);
            }
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
        Log.d(TAG, "Loading configuration from local: " + (this.dataWrapper != null ? "version " + this.dataWrapper.version : "NONE"));
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
                "lastSync=" + lastSync + " ("+ Util.delayFromNow((int)(lastSync/1000))+" seconds ago)" +
                ", minRemoteSyncDelay=" + minRemoteSyncDelay +
                '}';
    }

    public class SyncConfig{
        public int version;
        public DataType data;

        public SyncConfig() {
            this.version = 0;
            this.data = null;
        }
    }
}
