package com.timappweb.timapp.sync.performers;

import com.facebook.AccessToken;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.UserFriend;
import com.timappweb.timapp.events.SyncResultMessage;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.io.responses.ResponseSyncWrapper;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.sync.callbacks.InvitationSyncCallback;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.sync.exceptions.HttpResponseSyncException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;

/**
 * Created by Stephane on 04/09/2016.
 */
public class SyncFactory {

    public static FullTableSyncPerformer syncFriends(final SyncResultMessage syncResultMessage) {

        return new FullTableSyncPerformer(UserFriend.class)
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
                .setRemoteLoader(new DataSyncAdapter.RemoteLoader<ResponseSyncWrapper, UserFriend>() {
                    @Override
                    protected Call getCall(HashMap options) {
                        AccessToken.refreshCurrentAccessTokenAsync();
                        String accessToken = AccessToken.getCurrentAccessToken().getToken();
                        options.put("fb_access_token", accessToken);
                        return RestClient.service().friends(options);
                    }

                    @Override
                    protected List<UserFriend> getEntries(ResponseSyncWrapper body) {
                        syncResultMessage.setCount(body != null ? body.getCount() : 0);
                        return body.items;
                    }
                });
    }

    public static SyncPerformer syncInvitationsReceived(SyncAdapterOption options, final SyncResultMessage syncResultMessage) throws IOException, HttpResponseSyncException {
        return new MultipleEntriesSyncPerformer<EventsInvitation, ResponseSyncWrapper<EventsInvitation>>()
                .setLocalEntries(MyApplication.getCurrentUser().getInviteReceived())
                .setSyncOptions(options)
                .setRemoteLoader(new DataSyncAdapter.RemoteLoader<ResponseSyncWrapper<EventsInvitation>, EventsInvitation>() {
                    @Override
                    protected Call<ResponseSyncWrapper<EventsInvitation>> getCall(HashMap<String, String> options) {
                        return RestClient.service().inviteReceived(options);
                    }

                    @Override
                    protected List<EventsInvitation> getEntries(ResponseSyncWrapper<EventsInvitation> body) {
                        syncResultMessage.setUpToDate(body == null || body.getCount()  == 0);
                        return body.items;
                    }
                })
                .setCallback(new InvitationSyncCallback());
    }
}
