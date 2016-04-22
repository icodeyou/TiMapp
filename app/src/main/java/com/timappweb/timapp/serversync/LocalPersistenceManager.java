package com.timappweb.timapp.serversync;

/**
 * Created by stephane on 4/21/2016.
 */
public interface LocalPersistenceManager {

    public void write(SyncConfig data);
    public SyncConfig load();

    void clear();
}
