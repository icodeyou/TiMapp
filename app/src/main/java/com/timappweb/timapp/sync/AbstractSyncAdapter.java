package com.timappweb.timapp.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
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
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.SyncBaseModel;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by stephane on 4/27/2016.
 */
public abstract class AbstractSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";

    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

    /**
     * Sync interval in seconds
     */
    public static final int SYNC_INTERVAL = 60 * 180;

    /**
     *
     */
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private static Account mAccount;

    public AbstractSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public AbstractSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    public static void performModelSync(Class<? extends SyncBaseModel> classType, Call call, SyncResult syncResult){
        try {
            Log.d(TAG, "Synchronise type: " + classType);
            Response<List<? extends SyncBaseModel>> response = call.execute();
            if (response.isSuccess()){
                updateLocalDatabase(classType, response.body(), syncResult);
            }
            else {
                // TODO handle this case globally ???
                Log.e(TAG, "Cannot synchronise, api response invalid: " + response.code());
            }
        }
        catch (MalformedURLException e) {
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
    public static void updateLocalDatabase(Class<? extends SyncBaseModel> classType,
                                           final List<? extends SyncBaseModel> remoteEntries,
                                           final SyncResult syncResult)
            throws IOException, XmlPullParserException, RemoteException,
            OperationApplicationException, ParseException {

        Log.i(TAG, "Found " + remoteEntries.size() + " remote entrie(s)");
        if (remoteEntries.size() == 0){
            Log.i(TAG, "No data returned by web service => removing all local entries");
            new Delete().from(classType).execute();
            return;
        }

        // Build hash table of incoming entries
        HashMap<Long, SyncBaseModel> entryMap = new HashMap();
        for (SyncBaseModel m : remoteEntries) {
            entryMap.put(m.getSyncKey(), m);
        }
        // Get list of all items
        Log.i(TAG, "Fetching local entries for merge");

        // TODO use content provider:
        From query = new Select().from(classType);
        List<? extends SyncBaseModel> localEntries = query.execute();

        Log.i(TAG, "Found " + localEntries.size() + " local entries. Computing merge solution...");
        try
        {
            ActiveAndroid.beginTransaction();

            // Update and remove existing items
            for (SyncBaseModel localModel: localEntries){
                SyncBaseModel match = entryMap.get(localModel.getSyncKey());
                if (match != null) {
                    entryMap.remove(localModel.getSyncKey());
                    if (!match.isSync(localModel)){
                        match.save();
                        syncResult.stats.numUpdates++;
                        Log.i(TAG, "Updating: " + localModel.toString());
                    }
                    else{
                        Log.i(TAG, "No action: " + localModel.toString());
                    }
                }
                else{
                    Log.i(TAG, "Deleting: " + localModel.toString());
                    localModel.delete();
                    syncResult.stats.numDeletes++;
                }
            }

            // Add new items
            for (SyncBaseModel m : entryMap.values()) {
                Log.i(TAG, "Scheduling insert: " + m.toString());
                m.save();
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
     * Helper method to get the fake account to be used with ConfigSyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        if (mAccount != null){
            return mAccount;
        }
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        mAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(mAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(mAccount, "", null)) {
                Log.e(TAG, "Cannot add this account for authority");
                return null;
            }
            /*
             * @warning If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            ContentResolver.setIsSyncable(mAccount, context.getString(R.string.content_authority), 1);
            onAccountCreated(mAccount, context);
        }
        else{
            Log.i(TAG, "User account exists, OK");
        }
        return mAccount;
    }



    protected static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        ConfigSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

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

    public static void logSyncPending(Context context) {
        String authority = context.getString(R.string.content_authority);
        Account account = getSyncAccount(context);
        if (ContentResolver.isSyncPending(account, authority)  ||
                ContentResolver.isSyncActive(account, authority)) {
            Log.i(TAG, "ContentResolver SyncPending, canceling");
            ContentResolver.cancelSync(account, authority);
        }
    }
}

