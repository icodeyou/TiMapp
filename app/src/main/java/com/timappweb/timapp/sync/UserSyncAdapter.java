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

import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.UserQuota;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.sync.callbacks.UserQuotaSyncCallback;
import com.timappweb.timapp.sync.exceptions.CannotSyncException;
import com.timappweb.timapp.sync.performers.MultipleEntriesSyncPerformer;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CannotSyncException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "--------------- Network synchronization complete for user----------------------");
    }

    public void performUserQuotaSync(User user) throws IOException, CannotSyncException {
        new MultipleEntriesSyncPerformer<>(RestClient.service().userQuotas(), user.getQuotas(), 0, true)
                .setCallback(new UserQuotaSyncCallback())
                .perform();
    }

    public static void syncImmediately(Context context) {
        Log.i(TAG, "Requesting immediate sync for user data");
        syncImmediately(context, context.getString(R.string.content_authority_user));
    }
}
