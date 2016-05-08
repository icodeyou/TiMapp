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
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.sync.performers.FriendsSyncPerformer;
import com.timappweb.timapp.sync.performers.RemoteMasterSyncPerformer;
import com.timappweb.timapp.sync.performers.SingleEntrySyncPerformer;

import java.io.IOException;

/**
 * TODO sync adapter for user data
 */
public class UserSyncAdapter extends AbstractSyncAdapter {

    public static final String TAG = "MapDataAdapter";

    public static final String SYNC_TYPE_KEY = "data_sync_type";
    public static final String SYNC_ID_KEY = "data_sync_id";
    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public UserSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public UserSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within ConfigSyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them.
     .
     *
     * <p>This is where we actually perform any work required to perform a sync.
     * {@link AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to peform blocking I/O here.
     *
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the sync.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "--------------- Beginning network synchronization for data---------------------");

        Log.i(TAG, "--------------- Network synchronization complete for data----------------------");
    }

}
