package com.timappweb.timapp.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.timappweb.timapp.R;

/**
 * Created by stephane on 4/27/2016.
 */
public abstract class AbstractSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";

    /**
     * Network connection timeout, in milliseconds.
     */
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 20000;  // 20 seconds

    /**
     * Network read timeout, in milliseconds.
     */
    private static final int NET_READ_TIMEOUT_MILLIS = 15000;  // 15 seconds

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


    /**
     * Helper method to have the merge adapter merge immediately
     * @param context The context used to access the account service
     */
    public static void  syncImmediately(Context context, String authority, Bundle bundle) {
        Log.d(TAG, "Request sync immediately with authority=" + authority);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        ContentResolver.requestSync(getSyncAccount(context), authority, bundle);
    }
    /**
     * Helper method to have the merge adapter merge immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context, String authority) {
        Bundle bundle = new Bundle();
        syncImmediately(context, authority, bundle);
    }


    /**
     * Helper method to schedule the merge adapter periodic execution
     */
    public static void configurePeriodicSync(Context context,
                                             int syncInterval,
                                             int flexTime,
                                             String authority) {
        Account account = getSyncAccount(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic merge
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


    private static boolean hasAccount(Context context, String name) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccounts();
        for(Account account : accounts) {
            if(account.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method to get the fake account to be used with ConfigSyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     * @throws java.lang.SecurityException TODO handle this error
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
        if (!hasAccount(context, mAccount.name) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(mAccount, "", null)) {
                // TODO throw exception instead
                Log.e(TAG, "Cannot add this account for authority");
                return null;
            }
            /*
             * @warning If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            ContentResolver.setIsSyncable(mAccount, context.getString(R.string.content_authority_config), 1);
            ContentResolver.setIsSyncable(mAccount, context.getString(R.string.content_authority_data), 1);
            ContentResolver.setIsSyncable(mAccount, context.getString(R.string.content_authority_user), 1);

            onAccountCreated(mAccount, context);
        }
        else{
            Log.i(TAG, "User account exists, OK");
        }
        return mAccount;
    }



    protected static void onAccountCreated(Account newAccount, Context context) {
        String authority = context.getString(R.string.content_authority_config);
        /*
         * Since we've created an account
         */
        AbstractSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME, authority);

        /*
         * Without calling setSyncAutomatically, our periodic merge will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, authority, true);

        /*
         * Finally, let's do a merge to get things started
         */
        syncImmediately(context, authority);
    }

    public static void initializeSyncAdapter(Context context) {
        Log.d(TAG, "Initializing sync adapter");
        getSyncAccount(context);
    }

    public static void logSyncPending(Context context, String authority) {
        Account account = getSyncAccount(context);
        if (ContentResolver.isSyncPending(account, authority)  ||
                ContentResolver.isSyncActive(account, authority)) {
            Log.i(TAG, "ContentResolver SyncPending, canceling");
            ContentResolver.cancelSync(account, authority);
        }
    }

}

