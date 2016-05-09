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
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.query.From;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.MapAreaInfo;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.SyncHistory;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserPlace;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.PaginationResponse;
import com.timappweb.timapp.sync.performers.FriendsSyncPerformer;
import com.timappweb.timapp.sync.performers.InvitationsSyncPerformer;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;
import com.timappweb.timapp.sync.performers.RemoteMasterSyncPerformer;
import com.timappweb.timapp.sync.performers.SingleEntrySyncPerformer;

import java.io.IOException;
import java.util.List;

/**
 * Define a sync adapter for the app.
 *
 * <p>This class is instantiated in {@link ConfigSyncService}, which also binds DataSyncAdapter to the system.
 * DataSyncAdapter should only be initialized in ConfigSyncService, never anywhere else.
 *
 * <p>The system calls onPerformSync() via an RPC call through the IBinder object supplied by
 * ConfigSyncService.
 */
public class DataSyncAdapter extends AbstractSyncAdapter {

    public static final String TAG = "DataSyncAdapter";

    public static final String SYNC_TYPE_KEY = "data_sync_type";
    public static final String SYNC_ID_KEY = "data_sync_id";
    public static final String SYNC_LAST_TIME = "data_sync_time";

    public static final int SYNC_TYPE_FRIENDS = 1;
    public static final int SYNC_TYPE_EVENT_AROUD_USER = 2;
    public static final int SYNC_TYPE_USER = 3;
    public static final int SYNC_TYPE_PLACE = 4;
    private static final int SYNC_TYPE_PLACE_STATUS = 5;
    public static final int SYNC_TYPE_INVITE_SENT = 6;
    public static final int SYNC_TYPE_INVITE_RECEIVED = 7;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public DataSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public DataSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
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

        int syncTypeId = extras.getInt(DataSyncAdapter.SYNC_TYPE_KEY, -1);
        Log.i(TAG, "onPerformSync with type=" + syncTypeId);
        try {
            switch (syncTypeId){
                case DataSyncAdapter.SYNC_TYPE_FRIENDS:
                    new FriendsSyncPerformer(
                            RestClient.service().friends().execute().body().items,
                            MyApplication.getCurrentUser().getFriends(),
                            syncResult).perform();
                    break;
                case DataSyncAdapter.SYNC_TYPE_EVENT_AROUD_USER:
                    break;
                case DataSyncAdapter.SYNC_TYPE_INVITE_RECEIVED:
                    new InvitationsSyncPerformer(
                            RestClient.service().inviteReceived().execute().body().items,
                            MyApplication.getCurrentUser().getInviteReceived(),
                            syncResult).perform();
                    break;
                case DataSyncAdapter.SYNC_TYPE_INVITE_SENT:
                    //new InvitationsSyncPerformer(
                    //        RestClient.service().invitesSent().execute().body().items,
                    //        MyApplication.getCurrentUser().getInviteReceived(),
                    //        syncResult).perform();
                    break;
                case DataSyncAdapter.SYNC_TYPE_PLACE_STATUS:
                    new RemoteMasterSyncPerformer(
                            RestClient.service().placeStatus().execute().body(),
                            MyApplication.getCurrentUser().getPlaceStatus(),
                            syncResult).perform();
                    break;
                case DataSyncAdapter.SYNC_TYPE_USER:
                    int id = extras.getInt(SYNC_ID_KEY, -1);
                    if (id == -1) {
                        Log.e(TAG, "Invalid sync key. Please provide a sync key");
                        return; //throw new InvalidParameterException();
                    }
                    new SingleEntrySyncPerformer(User.class, id, RestClient.service().profile(id).execute(), syncResult).perform();
                    break;
                case DataSyncAdapter.SYNC_TYPE_PLACE:
                    id = extras.getInt(SYNC_ID_KEY, -1);
                    if (id == -1) {
                        Log.e(TAG, "Invalid sync key. Please provide a sync key");
                        return; //throw new InvalidParameterException();
                    }
                    new SingleEntrySyncPerformer(Place.class, id, RestClient.service().viewPlace(id).execute(), syncResult).perform();
                    break;
                default:
                    Log.e(TAG, "Invalid sync type id: " + syncTypeId);
                    return;
            }
            SyncHistory.updateSync(syncTypeId);

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "--------------- Network synchronization complete for data----------------------");
    }

}
