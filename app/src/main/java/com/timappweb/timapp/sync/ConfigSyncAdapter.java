/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.timappweb.timapp.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.entities.ApplicationRules;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.SpotCategory;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.UserQuota;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.sync.performers.RemoteMasterSyncPerformer;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Define a merge adapter for the app.
 *
 * <p>This class is instantiated in {@link ConfigSyncService}, which also binds ConfigSyncAdapter to the system.
 * ConfigSyncAdapter should only be initialized in ConfigSyncService, never anywhere else.
 *
 * <p>The system calls onPerformSync() via an RPC call through the IBinder object supplied by
 * ConfigSyncService.
 */
public class ConfigSyncAdapter extends AbstractSyncAdapter {

    public static final String TAG = "ConfigSyncAdapter";

    /**
     * Content resolver, for performing database operations.
     */
    //private final ContentResolver mContentResolver;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public ConfigSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        //mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public ConfigSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        //mContentResolver = context.getContentResolver();
    }

    /**
     * Called by the Android system in response to a request to run the merge adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within ConfigSyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them.
     .
     *
     * <p>This is where we actually perform any work required to perform a merge.
     * {@link AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to peform blocking I/O here.
     *
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the merge.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "--------------- Beginning network synchronization -----------------------------");
        // TODO paralelize this
        this.syncApplicationRules();
        this.performModelSync(SpotCategory.class,  RestClient.service().spotCategories(), syncResult);
        this.performModelSync(EventCategory.class, RestClient.service().eventCategories(), syncResult);
        Log.i(TAG, "--------------- Network synchronization complete -------------------------------");
    }

    public void performModelSync(Class<? extends SyncBaseModel> classType, Call remoteQuery, SyncResult syncResult){
        Log.i(TAG, "Performing model sync for " + classType.getCanonicalName() + "...");
        From localQuery = new Select().from(classType);
        try {
            Response response = remoteQuery.execute();
            if (response.isSuccessful()){
                List<? extends SyncBaseModel> remoteEntries = (List<? extends SyncBaseModel>) response.body();
                new RemoteMasterSyncPerformer(remoteEntries, localQuery.<SyncBaseModel>execute(), syncResult).perform();
            }
        }
        catch (IOException e) {
            Log.e(TAG, "Error performing sync for " + classType + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void syncApplicationRules() {
        try {
            Log.i(TAG, "Sync application rules...");
            ApplicationRules rules = RestClient.service().applicationRules().execute().body();
            ConfigurationProvider.setApplicationRules(rules);
        } catch (IOException e) {
            Log.e(TAG, "Cannot load application rules: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void syncImmediately(Context context) {
        syncImmediately(context, context.getString(R.string.content_authority_config));
    }
}
