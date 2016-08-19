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

package com.timappweb.timapp.sync.data;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventTag;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.Picture;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SyncHistory;
import com.timappweb.timapp.data.models.SyncHistoryBounds;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.data.models.UserFriend;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.events.SyncResultMessage;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.request.QueryCondition;
import com.timappweb.timapp.rest.io.responses.PaginatedResponse;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.sync.AbstractSyncAdapter;
import com.timappweb.timapp.sync.callbacks.InvitationSyncCallback;
import com.timappweb.timapp.sync.callbacks.PictureSyncCallback;
import com.timappweb.timapp.sync.callbacks.RemoteMasterSyncCallback;
import com.timappweb.timapp.sync.callbacks.UserPlaceSyncCallback;
import com.timappweb.timapp.sync.config.ConfigSyncService;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;
import com.timappweb.timapp.sync.exceptions.HttpResponseSyncException;
import com.timappweb.timapp.sync.exceptions.MissingSyncParameterException;
import com.timappweb.timapp.sync.performers.FullTableSyncPerformer;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;
import com.timappweb.timapp.sync.performers.SingleEntrySyncPerformer;
import com.timappweb.timapp.sync.performers.StoreEntriesSyncPerformer;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

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
    public static final String LAST_SYNC_TIME           = "data_sync_time";
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

        SyncAdapterOption options = new SyncAdapterOption(extras);
        Log.i(TAG, "Performing sync for type=" + options.getSyncType());
        try {
            SyncResultMessage syncResultMessage = new SyncResultMessage(options.getSyncType());
            switch (options.getSyncType()){
                case DataSyncAdapter.SYNC_TYPE_FRIENDS:
                    this.syncFriends(syncResultMessage);
                    break;
                case DataSyncAdapter.SYNC_TYPE_INVITE_RECEIVED:
                    this.syncInvitation(options, syncResultMessage);
                    break;
                case DataSyncAdapter.SYNC_TYPE_INVITE_SENT:
                    this.syncInviteSent(options, syncResultMessage);
                    break;
                case DataSyncAdapter.SYNC_TYPE_EVENT_STATUS:
                    new MultipleEntriesSyncPerformer<UserEvent, List<UserEvent>>()
                            .setLocalEntries(MyApplication.getCurrentUser().getPlaceStatus())
                            .setRemoteLoader(new RemoteLoader<List<UserEvent>, UserEvent>() {
                                @Override
                                protected Call getCall(HashMap options) {
                                    return RestClient.service().placeStatus();
                                }

                                @Override
                                protected List getEntries(@NotNull List body) {
                                    return body;
                                }
                            })
                            .setCallback(new RemoteMasterSyncCallback())
                            .perform();
                    break;
                case DataSyncAdapter.SYNC_TYPE_EVENT_PICTURE:
                    this.syncEventPicture(options, syncResultMessage);
                    break;
                case DataSyncAdapter.SYNC_TYPE_EVENT_USERS:
                    this.syncEventUsers(options, syncResultMessage);
                    break;
                case DataSyncAdapter.SYNC_TYPE_EVENT_INVITED:
                    this.syncEventInvited(options, syncResultMessage);
                    break;
                case DataSyncAdapter.SYNC_TYPE_EVENT_TAGS:
                    this.syncEventTags(options, syncResultMessage);
                    break;
                case DataSyncAdapter.SYNC_TYPE_USER:
                    long id = extractRemoteId(extras);
                    new SingleEntrySyncPerformer(User.class, id, RestClient.service().profile(id).execute(), syncResult).perform();
                    break;
                case DataSyncAdapter.SYNC_TYPE_EVENT:
                    id = extractRemoteId(extras);
                    new SingleEntrySyncPerformer(Event.class, id, RestClient.service().viewPlace(id).execute(), syncResult).perform();
                    break;
                case DataSyncAdapter.SYNC_TYPE_MULTIPLE_SPOT:
                    this.syncMultipleSpot(options, syncResultMessage);
                    break;
                case DataSyncAdapter.SYNC_TYPE_MULTIPLE_EVENT:
                    this.syncMultipleEvents(options, syncResultMessage);
                    break;
                default:
                    Log.e(TAG, "Invalid sync type id: " + options.getSyncType());
                    throw new CannotSyncException("Invalid sync type id: " + options.getSyncType(), options.getSyncType());
            }
            SyncHistory.updateSync(options.getSyncType(), options);
            EventBus.getDefault().post(syncResultMessage);

        } catch (IOException e) {
            Log.e(TAG, "IOException while performing sync. Do you have a network access ? Message: " + e.toString());
            EventBus.getDefault().post(e);
        }
        catch (CannotSyncException e) {
            Log.e(TAG, "Cannot sync: " + e.getMessage());
            EventBus.getDefault().post(e);
        }
        Log.v(TAG, "--------------- Network synchronization complete for data----------------------");
    }

    private void syncMultipleEvents(SyncAdapterOption options, SyncResultMessage syncTypeId) throws IOException, CannotSyncException {
        LatLngBounds bounds = extractMapBounds(options.getBundle());
        final QueryCondition conditions = new QueryCondition().setBounds(bounds);
        new MultipleEntriesSyncPerformer<Event, List<Event>>()
                .setRemoteLoader(new RemoteLoader<List<Event>, Event>() {
                    @Override
                    protected Call getCall(HashMap<String, String> options) {
                        return RestClient.service().bestPlaces(conditions.toMap());
                    }
                })
                .setLocalEntries(Event.findInArea(bounds, Event.class))
                .setCallback(new InvitationSyncCallback())
                .perform();
    }

    private void syncEventPicture(SyncAdapterOption options, SyncResultMessage result) throws CannotSyncException, IOException {
        final Event event = extractEvent(options.getBundle());
        final MultipleEntriesSyncPerformer performer = new MultipleEntriesSyncPerformer<Picture, ResponseSyncWrapper<Picture>>()
                .setRemoteLoader(new RemoteLoader<ResponseSyncWrapper<Picture>, Picture>() {
                    @Override
                    protected Call getCall(HashMap<String, String> options) {
                        return RestClient.service().viewPicturesForPlace(event.getRemoteId(), options);
                    }

                    @Override
                    protected List<Picture> getEntries(@NotNull ResponseSyncWrapper<Picture> body) {
                        return body.items;
                    }
                })
                .setLocalEntries(event.getPictures());
        performer
                .setCallback(new PictureSyncCallback(performer, event))
                .setSyncOptions(options)
                .perform();

    }

    private void syncFriends(final SyncResultMessage syncResultMessage) throws IOException, HttpResponseSyncException {
        FullTableSyncPerformer syncPerformer = new FullTableSyncPerformer(UserFriend.class)
                .setCallback(new FullTableSyncPerformer.Callback<UserFriend>() {
                        @Override
                    public boolean beforeSave(UserFriend entry) {
                        entry.userSource = MyApplication.getCurrentUser();
                        return true;
                    }

                    @Override
                    public void afterSave(UserFriend entry) {
                    }
                })
                .setRemoteLoader(new RemoteLoader<ResponseSyncWrapper, UserFriend>() {
                    @Override
                    protected Call getCall(HashMap options) {
                        return RestClient.service().friends(options);
                    }

                    @Override
                    protected List<UserFriend> getEntries(ResponseSyncWrapper body) {
                        syncResultMessage.count = body != null ? body.getCount() : 0;
                        return body.items;
                    }
                });
        syncPerformer.perform();
    }
    private void syncEventUsers(SyncAdapterOption options, SyncResultMessage result) throws IOException, CannotSyncException {
        final Event event = extractEvent(options.getBundle());
        new MultipleEntriesSyncPerformer<UserEvent, PaginatedResponse<UserEvent>>()
                .setRemoteLoader(new RemoteLoader<PaginatedResponse<UserEvent>, UserEvent>() {
                    @Override
                    protected Call<PaginatedResponse<UserEvent>> getCall(HashMap<String, String> options) {
                        return RestClient.service().viewUsersForPlace(event.getRemoteId());
                    }

                    @Override
                    protected List<UserEvent> getEntries(@NotNull PaginatedResponse<UserEvent> body) {
                        return body.items;
                    }
                })
                .setLocalEntries(event.getUsers())
                .setCallback(new UserPlaceSyncCallback(event))
                .perform();
    }


    private void syncEventTags(SyncAdapterOption options, SyncResultMessage result) throws IOException, CannotSyncException {
        final Event event = extractEvent(options.getBundle());
        new StoreEntriesSyncPerformer<Tag, PaginatedResponse<Tag>>()
                .setRemoteLoader(new RemoteLoader<PaginatedResponse<Tag>, Tag>() {
                    @Override
                    protected Call getCall(HashMap<String, String> options) {
                        return RestClient.service().viewPopularTagsForPlace(event.getRemoteId());
                    }

                    @Override
                    protected List getEntries(@NotNull PaginatedResponse body) {
                        return body.items;
                    }
                })
                .setCallback(new StoreEntriesSyncPerformer.Callback<Tag>() {
                    @Override
                    public void save(Tag remoteModel) throws CannotSaveModelException {
                        EventTag eventTag = new EventTag(event, remoteModel, remoteModel.count_ref);
                        eventTag.deepSave();
                    }

                    @Override
                    public void before() {
                        new Delete().from(EventTag.class).where("Event = ?", event).execute();
                    }

                    @Override
                    public void after() {

                    }
                })
                .perform();
    }

    private void syncInviteSent(SyncAdapterOption options, SyncResultMessage result) throws IOException, CannotSyncException {
        final Event event = extractEvent(options.getBundle());
        new MultipleEntriesSyncPerformer<EventsInvitation, PaginatedResponse<EventsInvitation>>()
                .setRemoteLoader(new RemoteLoader<PaginatedResponse<EventsInvitation>, EventsInvitation>() {
                    @Override
                    protected Call getCall(HashMap<String, String> options) {
                        return  RestClient.service().invitesSent(event.getRemoteId());
                    }

                    @Override
                    protected List getEntries(@NotNull PaginatedResponse body) {
                        return body.items;
                    }
                })
                .setLocalEntries(MyApplication.getCurrentUser().getInviteSent(event.getId()))
                .setCallback(new RemoteMasterSyncCallback())
                .perform();
    }

    private void syncEventInvited(SyncAdapterOption options, SyncResultMessage result) throws CannotSyncException, IOException {
        new MultipleEntriesSyncPerformer<EventsInvitation, PaginatedResponse<EventsInvitation>>()
                .setRemoteLoader(new RemoteLoader<PaginatedResponse<EventsInvitation>, EventsInvitation>() {
                    @Override
                    protected Call getCall(HashMap<String, String> options) {
                        return  RestClient.service().inviteReceived(options);
                    }

                    @Override
                    protected List getEntries(@NotNull PaginatedResponse body) {
                        return body.items;
                    }
                })
                .setLocalEntries(MyApplication.getCurrentUser().getInviteReceived())
                .setCallback(new RemoteMasterSyncCallback())
                .perform();

    }

    private void syncMultipleSpot(SyncAdapterOption options, SyncResultMessage result) throws IOException, CannotSyncException, HttpResponseSyncException {
        LatLngBounds bounds = extractMapBounds(options.getBundle());
        final QueryCondition conditions = new QueryCondition().setBounds(bounds);
        new MultipleEntriesSyncPerformer<Spot, PaginatedResponse<Spot>>()
                .setRemoteLoader(new RemoteLoader<PaginatedResponse<Spot>, Spot>() {
                    @Override
                    protected Call getCall(HashMap<String, String> options) {
                        return  RestClient.service().spots(conditions.toMap());
                    }

                    @Override
                    protected List getEntries(@NotNull PaginatedResponse body) {
                        return body.items;
                    }
                })
                .setLocalEntries(Spot.findInArea(bounds, Spot.class))
                .setCallback(new RemoteMasterSyncCallback())
                .perform();
    }





    public void syncInvitation(SyncAdapterOption options, final SyncResultMessage syncResultMessage) throws IOException, CannotSyncException {
        new MultipleEntriesSyncPerformer<EventsInvitation, ResponseSyncWrapper<EventsInvitation>>()
                .setLocalEntries(MyApplication.getCurrentUser().getInviteReceived())
                .setSyncOptions(options)
                .setRemoteLoader(new RemoteLoader<ResponseSyncWrapper<EventsInvitation>, EventsInvitation>() {
                    @Override
                    protected Call<ResponseSyncWrapper<EventsInvitation>> getCall(HashMap<String, String> options) {
                        return RestClient.service().inviteReceived(options);
                    }

                    @Override
                    protected List<EventsInvitation> getEntries(ResponseSyncWrapper<EventsInvitation> body) {
                        syncResultMessage.count = body != null ? body.getCount() : 0;
                        return body.items;
                    }
                })
                .setCallback(new InvitationSyncCallback())
                .perform();
    }

    // ---------------------------------------------------------------------------------------------

    private LatLngBounds extractMapBounds(Bundle bundle) {
        LatLng sw = new LatLng(
                bundle.getDouble(SYNC_PARAM_MAP_BOUNDS + "swlatitude"),
                bundle.getDouble(SYNC_PARAM_MAP_BOUNDS + "swlongitude"));
        LatLng ne = new LatLng(
                bundle.getDouble(SYNC_PARAM_MAP_BOUNDS + "nelatitude"),
                bundle.getDouble(SYNC_PARAM_MAP_BOUNDS + "nelongitude"));
        return new LatLngBounds(sw, ne);
    }


    private Event extractEvent(Bundle extras) throws CannotSyncException {
        long eventId = extras.getLong(SYNC_PARAM_EVENT_ID, -1);
        if (eventId == -1) {
            Log.e(TAG, "Invalid sync key. Please provide a sync key");
            throw new MissingSyncParameterException(SYNC_PARAM_EVENT_ID);
        }
        Event event = Event.loadByRemoteId(Event.class, eventId);
        if (event == null){
            throw new CannotSyncException("Event id '" + eventId + "' does not exist", 0);
        }
        return event;
    }

    private long extractRemoteId(Bundle extras){
        int id = extras.getInt(SYNC_ID_KEY, -1);
        if (id == -1) {
            Log.e(TAG, "Invalid sync key. Please provide a sync key");
            throw new InvalidParameterException();
        }
        return id;
    }

    // ---------------------------------------------------------------------------------------------

    public abstract static class RemoteLoader<BodyType, EntityType>{

        protected Response<BodyType> response;

        protected abstract Call<BodyType> getCall(HashMap<String, String> options);

        public BodyType load(@NotNull HashMap<String, String> options) throws IOException, HttpResponseSyncException {
            if (response != null) return response.body();
            response = getCall(options).execute();
            if (response.isSuccessful()){
                return response.body();
            }
            throw new HttpResponseSyncException(response, options);
        }

        public List<EntityType> items(@NotNull HashMap<String, String> options) throws IOException, HttpResponseSyncException {
            return getEntries(load(options));
        }

        protected List<EntityType> getEntries(@NotNull BodyType body){
            return (List<EntityType>) body;
        }

        public Response<BodyType> getResponse(){
            return this.response;
        }

    }

}

