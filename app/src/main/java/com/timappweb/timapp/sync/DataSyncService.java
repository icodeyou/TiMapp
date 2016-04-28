package com.timappweb.timapp.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/** Service to handle sync requests.
 *
 * <p>This service is invoked in response to Intents with action android.content.ConfigSyncAdapter, and
 * returns a Binder connection to ConfigSyncAdapter.
 *
 * <p>For performance, only one sync adapter will be initialized within this application's context.
 *
 * <p>Note: The ConfigSyncService itself is not notified when a new sync occurs. It's role is to
 * manage the lifecycle of our {@link ConfigSyncAdapter} and provide a handle to said ConfigSyncAdapter to the
 * OS on request.
 */
public class DataSyncService extends Service {
    private static final String TAG = "DataSyncService";

    private static final Object sSyncAdapterLock = new Object();
    private static DataSyncAdapter sDataSyncAdapter = null;

    /**
     * Thread-safe constructor, creates static {@link ConfigSyncAdapter} instance.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
        synchronized (sSyncAdapterLock) {
            if (sDataSyncAdapter == null) {
                sDataSyncAdapter = new DataSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    /**
     * Logging-only destructor.
     */
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }

    /**
     * Return Binder handle for IPC communication with {@link ConfigSyncAdapter}.
     *
     * <p>New sync requests will be sent directly to the ConfigSyncAdapter using this channel.
     *
     * @param intent Calling intent
     * @return Binder handle for {@link ConfigSyncAdapter}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return sDataSyncAdapter.getSyncAdapterBinder();
    }
}
