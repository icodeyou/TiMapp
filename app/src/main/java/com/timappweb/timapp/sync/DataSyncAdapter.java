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
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SyncHistory;
import com.timappweb.timapp.data.models.SyncHistoryBounds;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserFriend;
import com.timappweb.timapp.events.SyncResultMessage;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;
import com.timappweb.timapp.rest.io.request.QueryCondition;
import com.timappweb.timapp.rest.io.responses.TableSyncResult;
import com.timappweb.timapp.sync.callbacks.EventTagsSyncCallback;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;
import com.timappweb.timapp.sync.performers.ChunckTableSyncPerformer;
import com.timappweb.timapp.sync.performers.FullTableSyncPerformer;
import com.timappweb.timapp.sync.callbacks.InvitationSyncCallback;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;
import com.timappweb.timapp.sync.callbacks.RemoteMasterSyncCallback;
import com.timappweb.timapp.sync.performers.SingleEntrySyncPerformer;
import com.timappweb.timapp.sync.callbacks.UserPlaceSyncCallback;
import com.timappweb.timapp.sync.performers.SyncAdapterOption;
import com.timappweb.timapp.utils.Util;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;

/**
 * Define a merge adapter for the app.
 *
 * <p>This class is instantiated in {@link ConfigSyncService}, which also binds DataSyncAdapter to the system.
 * DataSyncAdapter should only be initialized in ConfigSyncService, never anywhere else.
 *
 * <p>The system calls onPerformSync() via an RPC call through the IBinder object supplied by
 * ConfigSyncService.
 */
public class DataSyncAdapter extends AbstractSyncAdapter {

    public static final String TAG                      = "DataSyncAdapter";

    // ---------------------------------------------------------------------------------------------

    public static final String SYNC_TYPE_KEY            = "data_sync_type";
    public static final String SYNC_ID_KEY              = "data_sync_id";
    public static final String LAST_SYNC_TIME = "data_sync_time";
    public static final String SYNC_PARAM_EVENT_ID      = "place_id";
    public static final String SYNC_PARAM_MAP_BOUNDS    = "map_bounds";

    // ---------------------------------------------------------------------------------------------

    public static final int SYNC_TYPE_FRIENDS           = 1;
    public static final int SYNC_TYPE_MULTIPLE_EVENT    = 2;
    public static final int SYNC_TYPE_USER              = 3;
    public static final int SYNC_TYPE_EVENT             = 4;
    public static final int SYNC_TYPE_EVENT_USERS       = 8;
    public static final int SYNC_TYPE_EVENT_STATUS      = 5;
    public static final int SYNC_TYPE_INVITE_SENT       = 6;
    public static final int SYNC_TYPE_INVITE_RECEIVED   = 7;
    public static final int SYNC_TYPE_EVENT_INVITED     = 9;
    public static final int SYNC_TYPE_EVENT_PICTURE     = 10;
    public static final int SYNC_TYPE_EVENT_TAGS        = 11;
    public static final int SYNC_TYPE_MULTIPLE_SPOT     = 12;

    // ---------------------------------------------------------------------------------------------

    public static final String ACTION_SYNC_EVENT_FINISHED  = "com.timappweb.timapp.ACTION_SYNC_EVENT_FINISHED";
    public static final String ACTION_SYNC_EVENT_PICTURES = "com.timappweb.timapp.ACTION_SYNC_EVENT_PICTURES";

    // ---------------------------------------------------------------------------------------------

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
        Log.v(TAG, "--------------- Beginning network synchronization for data---------------------");

        int syncTypeId = extras.getInt(DataSyncAdapter.SYNC_TYPE_KEY, -1);
        Log.i(TAG, "Performing sync for type=" + syncTypeId);
        try {
            LatLngBounds bounds;

            // TODO MANAGE ERRORS .........
            switch (syncTypeId){
                case DataSyncAdapter.SYNC_TYPE_FRIENDS:
                    this.syncFriends(extras);
                case DataSyncAdapter.SYNC_TYPE_INVITE_RECEIVED:
                    this.syncInvitation(extras, syncTypeId);
                    break;
                case DataSyncAdapter.SYNC_TYPE_INVITE_SENT:
                    this.syncInviteSent(extras, syncTypeId);
                    break;
                case DataSyncAdapter.SYNC_TYPE_EVENT_STATUS:
                    new MultipleEntriesSyncPerformer<>(
                            RestClient.service().placeStatus(),
                            MyApplication.getCurrentUser().getPlaceStatus(),
                            DataSyncAdapter.SYNC_TYPE_EVENT_STATUS,
                            true)
                            .setCallback(new RemoteMasterSyncCallback())
                            .perform();
                    break;
                case DataSyncAdapter.SYNC_TYPE_EVENT_PICTURE:
                    this.syncEventPicture(extras, syncTypeId);
                    break;
                case DataSyncAdapter.SYNC_TYPE_EVENT_USERS:
                    this.syncEventUsers(extras, syncTypeId);
                    break;
                case DataSyncAdapter.SYNC_TYPE_EVENT_INVITED:
                    this.syncEventInvited(extras, syncTypeId);
                    break;
                case DataSyncAdapter.SYNC_TYPE_EVENT_TAGS:
                    this.syncEventTags(extras, syncTypeId);
                    break;
                case DataSyncAdapter.SYNC_TYPE_USER:
                    long id = extractRemoteId(extras);
                    new SingleEntrySyncPerformer(User.class, id, RestClient.service().profile(id).execute(), syncResult).perform();
                    break;
                case DataSyncAdapter.SYNC_TYPE_EVENT:
                    id = extractRemoteId(extras);
                    new SingleEntrySyncPerformer(Event.class, id, RestClient.service().viewPlace(id).execute(), syncResult).perform();
                    getContext().sendBroadcast(new Intent(ACTION_SYNC_EVENT_FINISHED));
                    break;
                case DataSyncAdapter.SYNC_TYPE_MULTIPLE_SPOT:
                    this.syncMultipleSpot(extras, syncTypeId);
                    break;
                case DataSyncAdapter.SYNC_TYPE_MULTIPLE_EVENT:
                    this.syncMultipleEvents(extras, syncTypeId);
                    break;
                default:
                    Log.e(TAG, "Invalid sync type id: " + syncTypeId);
                    return;
            }
            SyncHistory.updateSync(syncTypeId);

        } catch (IOException e) {
            Log.e(TAG, "IOException while performing sync. Do you have a network access ? Message: " + e.toString());
            e.printStackTrace();
        } catch (CannotSyncException e) {
            Log.e(TAG, "Cannot sync: " + e.getMessage());
            e.printStackTrace();
        }
        Log.v(TAG, "--------------- Network synchronization complete for data----------------------");
    }

    private void syncMultipleEvents(Bundle extras, int syncTypeId) throws IOException, CannotSyncException {
        LatLngBounds bounds = extractMapBounds(extras);
        if (bounds != null && bounds.northeast != null){
            QueryCondition conditions = new QueryCondition().setBounds(bounds);
            Call<List<Event>> call = RestClient.service().bestPlaces(conditions.toMap());
            new MultipleEntriesSyncPerformer<>(
                        call,
                        Event.findInArea(bounds, Event.class),
                        syncTypeId,
                        true)
                        .perform();
            SyncHistoryBounds.updateSync(DataSyncAdapter.SYNC_TYPE_MULTIPLE_EVENT, bounds);
        }
        else{
            Util.appStateError(TAG, "Not bounds where given for the spot udpdate. Please fix this ASAP");
        }
    }

    private void syncEventPicture(Bundle extras, int syncTypeId) {
        Event event = extractEvent(extras);
        if (event == null) { Util.appStateError(TAG, "Event should not be null in sync adapter");}
        ChunckTableSyncPerformer pictureSyncPerformer = getEventPictureSyncPerformer(event);
        pictureSyncPerformer.setSyncParam(new SyncAdapterOption(extras));
        pictureSyncPerformer.perform();
        SyncResultMessage syncResultMessage = new SyncResultMessage(DataSyncAdapter.SYNC_TYPE_EVENT_PICTURE);
        syncResultMessage.setUpToDate(pictureSyncPerformer.getResult().up_to_date);
        EventBus.getDefault().post(syncResultMessage);
    }

    private void syncFriends(Bundle extras) {
        FullTableSyncPerformer syncPerformer = new FullTableSyncPerformer(UserFriend.class, new FullTableSyncPerformer.Callback<UserFriend>() {
            @Override
            public boolean beforeSave(UserFriend entry) {
                entry.userSource = MyApplication.getCurrentUser();
                return true;
            }
            @Override
            public void afterSave(UserFriend entry) {}
        }, new FullTableSyncPerformer.RemoteLoader() {
            @Override
            public TableSyncResult load(HashMap options) throws IOException {
                return (TableSyncResult) RestClient.service().friends(options).execute().body();
            }
        });
        syncPerformer.perform();
    }

    private void syncEventUsers(Bundle extras, int syncTypeId) throws IOException, CannotSyncException {

        Event event = extractEvent(extras);
        if (event == null) return;
        new MultipleEntriesSyncPerformer<>(
                RestClient.service().viewUsersForPlace(event.getRemoteId()),
                event.getUsers(),
                syncTypeId)
                .setCallback(new UserPlaceSyncCallback(event))
                .perform();
    }

    private void syncEventTags(Bundle extras, int syncTypeId) throws IOException, CannotSyncException {
        Event event = extractEvent(extras);
        if (event == null) return;
        new MultipleEntriesSyncPerformer<>(
                RestClient.service().viewPopularTagsForPlace(event.getRemoteId()),
                event.getTagsQuery().<Tag>execute(),
                syncTypeId,
                true)
                .setCallback(new EventTagsSyncCallback(event))
                .perform();
    }

    private void syncInviteSent(Bundle extras, int syncTypeId) throws IOException, CannotSyncException {
        Event event = extractEvent(extras);
        if (event == null) return;
        new MultipleEntriesSyncPerformer<EventsInvitation>(
                RestClient.service().invitesSent(event.getRemoteId()),
                MyApplication.getCurrentUser().getInviteSent(event.getId()),
                syncTypeId)
                .setCallback(new InvitationSyncCallback())
                .perform();
    }

    private void syncEventInvited(Bundle extras, int syncId) throws CannotSyncException, IOException {
        Event event = extractEvent(extras);
        if (event == null) throw new CannotSyncException("Missing event parameter", syncId);


        new MultipleEntriesSyncPerformer<EventsInvitation>(
                RestClient.service().invitesSent(event.getRemoteId()),
                MyApplication.getCurrentUser().getInviteSent(event.getId()),
                syncId)
                .perform();
    }

    private void syncMultipleSpot(Bundle extras, int syncId) throws IOException, CannotSyncException {
        LatLngBounds bounds = extractMapBounds(extras);
        if (bounds != null && bounds.northeast != null){
            QueryCondition conditions = new QueryCondition().setBounds(bounds);
            Call<PaginatedResponse<Spot>> request = RestClient.service().spots(conditions.toMap());
            MultipleEntriesSyncPerformer<Spot> syncPerformer
                    = new MultipleEntriesSyncPerformer(request, Spot.findInArea(bounds, Spot.class), syncId);
            syncPerformer.perform();
            SyncHistoryBounds.updateSync(DataSyncAdapter.SYNC_TYPE_MULTIPLE_SPOT, bounds);
        }
        else{
            Util.appStateError(TAG, "Not bounds where given for the spot udpdate. Please fix this ASAP");
        }

    }

    private LatLngBounds extractMapBounds(Bundle bundle) {
        LatLng sw = new LatLng(
                bundle.getDouble(SYNC_PARAM_MAP_BOUNDS + "swlatitude"),
                bundle.getDouble(SYNC_PARAM_MAP_BOUNDS + "swlongitude"));
        LatLng ne = new LatLng(
                bundle.getDouble(SYNC_PARAM_MAP_BOUNDS + "nelatitude"),
                bundle.getDouble(SYNC_PARAM_MAP_BOUNDS + "nelongitude"));
        return new LatLngBounds(sw, ne);
    }


    private Event extractEvent(Bundle extras){
        long eventId = extras.getLong(SYNC_PARAM_EVENT_ID, -1);
        if (eventId == -1) {
            Log.e(TAG, "Invalid sync key. Please provide a sync key");
            return null; //throw new InvalidParameterException();
        }
        return Event.loadByRemoteId(Event.class, eventId);
    }

    private long extractRemoteId(Bundle extras){
        int id = extras.getInt(SYNC_ID_KEY, -1);
        if (id == -1) {
            Log.e(TAG, "Invalid sync key. Please provide a sync key");
            throw new InvalidParameterException();
        }
        return id;
    }


    public ChunckTableSyncPerformer getEventPictureSyncPerformer(final Event event) {
        ChunckTableSyncPerformer performer = new ChunckTableSyncPerformer(Picture.class);
        performer.setRemoteLoader(new ChunckTableSyncPerformer.RemoteLoader<Picture>(){

            @Override
            public TableSyncResult<Picture> load(HashMap options) throws IOException {
                return (TableSyncResult<Picture>) RestClient.service().viewPicturesForPlace(event.getRemoteId(), options).execute().body();
            }

        });

        performer.setCallback(new ChunckTableSyncPerformer.Callback<Picture>() {
            @Override
            public boolean beforeSave(Picture entry, TableSyncResult<Picture> result) {
                entry.base_url = result.extra.getAsJsonObject().get("base_url").getAsString();
                entry.event = event;
                return true;
            }

            @Override
            public void afterSave(Picture entry, TableSyncResult<Picture> result) {

            }

        });
        return performer;
    }


    public void syncInvitation(Bundle extras, int syncTypeId) throws IOException, CannotSyncException {
        new MultipleEntriesSyncPerformer<EventsInvitation>(
                RestClient.service().inviteReceived(),
                MyApplication.getCurrentUser().getInviteReceived(),
                syncTypeId)
                .setCallback(new InvitationSyncCallback())
                .perform();
    }
}

