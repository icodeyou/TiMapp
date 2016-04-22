package com.timappweb.timapp.serversync;

/**
 * Created by stephane on 4/21/2016.
 */
public interface LocalPersistenceManager {

    public void write(SyncConfigManager.SyncConfig data);
    public SyncConfigManager.SyncConfig load();

}
