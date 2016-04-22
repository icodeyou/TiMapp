package com.timappweb.timapp.serversync;

import com.timappweb.timapp.rest.RestCallback;

import retrofit2.Callback;

/**
 * Created by stephane on 4/21/2016.
 */
public interface RemotePersistenceManager {

    public int version();
    public SyncConfig load(int currentVersion) throws CannotLoadException;


    class CannotLoadException extends Exception {
        public CannotLoadException(String s) {

        }
    }
}
