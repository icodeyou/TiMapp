package com.timappweb.timapp.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Service to handle user sync requests.
 */
public class UserSyncService extends Service {
    private static final String TAG = "UserSyncService";

    private static final Object sSyncAdapterLock = new Object();
    private static UserSyncAdapter sSyncAdapter = null;

    /**
     * Thread-safe constructor, creates static {@link ConfigSyncAdapter} instance.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new UserSyncAdapter(getApplicationContext(), true);
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
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
