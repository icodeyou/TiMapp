package com.timappweb.timapp.rest.services;

import com.timappweb.timapp.serversync.SyncConfig;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by stephane on 8/20/2015.
 */
public interface ConfigInterface {

    @GET("configurations/{type}/{version}")
    Call<SyncConfig> get(@Path("type") String type, @Query("version") int currentVersion);

}
