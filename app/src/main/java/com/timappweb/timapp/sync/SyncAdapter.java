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
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.QuotaType;
import com.timappweb.timapp.rest.RestClient;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Define a sync adapter for the app.
 *
 * <p>This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else.
 *
 * <p>The system calls onPerformSync() via an RPC call through the IBinder object supplied by
 * SyncService.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";

    /**
     * URL to fetch content from during a sync.
     *
     * <p>This points to the Android Developers Blog. (Side note: We highly recommend reading the
     * Android Developer Blog to stay up to date on the latest Android platform developments!)
     */
    private static final String FEED_URL = "http://android-developers.blogspot.com/atom.xml";

    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
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
        Log.i(TAG, "Beginning network synchronization");
        try {
            try {
                Log.i(TAG, "Streaming data from network: ");

                Call<List<QuotaType>> call = RestClient.service().quotaTypes();
                Response<List<QuotaType>> response = call.execute();
                List<QuotaType> quotaTypes = response.body();

                updateLocalFeedData(quotaTypes, syncResult);
            } finally {

            }
        } catch (MalformedURLException e) {
            Log.wtf(TAG, "Feed URL is malformed", e);
            syncResult.stats.numParseExceptions++;
            return;
        } catch (IOException e) {
            Log.e(TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Error parsing feed: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing feed: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
        } catch (RemoteException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        } catch (OperationApplicationException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        }
        Log.i(TAG, "Network synchronization complete");
    }

    /**
     * Read XML from an input stream, storing it into the content provider.
     *
     * <p>This is where incoming data is persisted, committing the results of a sync. In order to
     * minimize (expensive) disk operations, we compare incoming data with what's already in our
     * database, and compute a merge. Only changes (insert/update/delete) will result in a database
     * write.
     *
     * <p>As an additional optimization, we use a batch operation to perform all database writes at
     * once.
     *
     * <p>Merge strategy:
     * 1. Get cursor to all items in feed<br/>
     * 2. For each item, check if it's in the incoming data.<br/>
     *    a. YES: Remove from "incoming" list. Check if data has mutated, if so, perform
     *            database UPDATE.<br/>
     *    b. NO: Schedule DELETE from database.<br/>
     * (At this point, incoming database only contains missing items.)<br/>
     * 3. For any items remaining in incoming list, ADD to database.
     */
    public void updateLocalFeedData(final List<? extends Model> remoteEntries, final SyncResult syncResult)
            throws IOException, XmlPullParserException, RemoteException,
            OperationApplicationException, ParseException {


        Log.i(TAG, "Found " + remoteEntries.size() + " remote entrie(s)");
        // Build hash table of incoming entries
        HashMap<Long, Model> entryMap = new HashMap();
        for (Model m : remoteEntries) {
            entryMap.put(m.getId(), m);
        }
        // Get list of all items
        Log.i(TAG, "Fetching local entries for merge");

        // TODO use content provider:
        From query = new Select().from(QuotaType.class);
        List<QuotaType> localEntries = query.execute();

        Log.i(TAG, "Found " + localEntries.size() + " local entries. Computing merge solution...");

        try
        {
            ActiveAndroid.beginTransaction();

            // Update and remove existing items
            for (QuotaType quotaType: localEntries){
                Model match = entryMap.get(quotaType.getId());
                if (match != null) {
                    entryMap.remove(quotaType.getId());
                    if (!match.equals(quotaType)){
                        match.save();
                        syncResult.stats.numUpdates++;
                        Log.i(TAG, "Updating: " + quotaType.getId());
                    }
                    else{
                        Log.i(TAG, "No action: " + quotaType.getId());
                    }
                }
                else{
                    Log.i(TAG, "Deleting: " + quotaType.getId());
                    syncResult.stats.numDeletes++;
                }
            }

            // Add new items
            for (Model m : entryMap.values()) {
                Log.i(TAG, "Scheduling insert: entry_id=" + m.getId());
                m.delete();
                syncResult.stats.numInserts++;
            }

            Log.i(TAG, "Merge solution ready. Applying updates");
            ActiveAndroid.setTransactionSuccessful();
        }
        finally
        {
            Log.i(TAG, "Merge solution done");
            ActiveAndroid.endTransaction();
        }

        // mContentResolver.notifyChange(
        //        FeedContract.Entry.CONTENT_URI, // URI where data was modified
        //        null,                           // No local observer
        //        false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }



    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.d(TAG, "Request sync immediately");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        Log.d(TAG, "Initializing sync adapter");
        getSyncAccount(context);
    }
}
