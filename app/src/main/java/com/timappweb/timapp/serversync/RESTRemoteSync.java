package com.timappweb.timapp.serversync;

import com.timappweb.timapp.rest.services.ConfigInterface;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by stephane on 4/22/2016.
 */
public class RESTRemoteSync implements RemotePersistenceManager {

    private final ConfigInterface service;
    private final String path;

    public RESTRemoteSync(String path, ConfigInterface service) {
        this.service = service;
        this.path = path;
    }

    @Override
    public int version() {
        // TODO
        return 0;
    }

    @Override
    public SyncConfigManager.SyncConfig load(int currentVersion) throws CannotLoadException {
        Call call = service.get(this.path, currentVersion);
        try {
            Response response = call.execute();
            if (response.isSuccess()){
                return (SyncConfigManager.SyncConfig) response.body();
            }
            else{
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new CannotLoadException("Cannot load sys configuration from api");
    }

}
