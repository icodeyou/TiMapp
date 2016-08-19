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

package com.timappweb.timapp.sync.user;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserQuota;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.sync.AbstractSyncAdapter;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;
import com.timappweb.timapp.sync.exceptions.HttpResponseSyncException;
import com.timappweb.timapp.sync.performers.StoreEntriesSyncPerformer;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;

/**
 *
 */
public class UserSyncAdapter extends AbstractSyncAdapter {

    public static final String TAG = "UserSyncAdapter";

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
        if (!MyApplication.isLoggedIn()){
            Log.e(TAG, "Call user sync adapter but user is not logged in");
            return;
        }
        Log.i(TAG, "--------------- Beginning network synchronization for user---------------------");
        try {
            this.performUserQuotaSync(MyApplication.getCurrentUser());
        }
        catch (IOException e) {
            Log.e(TAG, "IOException while performing sync. Do you have a network access ? Message: " + e.toString());
            //EventBus.getDefault().post(e);
        }
        catch (CannotSyncException e) {
            Log.e(TAG, "Cannot sync: " + e.getMessage());
           // EventBus.getDefault().post(e);
        }
        Log.i(TAG, "--------------- Network synchronization complete for user----------------------");
    }

    public void performUserQuotaSync(final User user) throws IOException, CannotSyncException, HttpResponseSyncException {

        new StoreEntriesSyncPerformer<UserQuota, List<UserQuota>>()
                .setRemoteLoader(new DataSyncAdapter.RemoteLoader<List<UserQuota>, UserQuota>(){
                    @Override
                    protected Call getCall(HashMap<String, String> options) {
                        return RestClient.service().userQuotas();
                    }

                    @Override
                    protected List<UserQuota> getEntries(List<UserQuota> body) {
                        return body;
                    }
                })
                .setCallback(new StoreEntriesSyncPerformer.Callback<UserQuota>() {
                    @Override
                    public void save(UserQuota remoteModel) throws CannotSaveModelException {
                        Log.d(TAG, "    - " + remoteModel);
                        remoteModel.user = user;
                        remoteModel.mySave();
                    }

                    @Override
                    public void before() {
                        new Delete().from(UserQuota.class).where("User = ?", user).execute();
                        Log.d(TAG, "Cleaning user quota... Fetching new quota");
                    }

                    @Override
                    public void after() {
                        Log.d(TAG, "New quota have been updated");}
                })
                .perform();
    }

    public static void syncImmediately(Context context) {
        Log.i(TAG, "Requesting immediate sync for user data");
        syncImmediately(context, context.getString(R.string.content_authority_user));
    }
}
