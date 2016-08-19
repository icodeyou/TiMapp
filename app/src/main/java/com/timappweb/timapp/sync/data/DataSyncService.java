package com.timappweb.timapp.sync.data;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/** Service to handle merge requests.
 *
 * <p>This service is invoked in response to Intents with action android.content.DataSyncAdapter, and
 * returns a Binder connection to DataSyncAdapter.
 *
 * <p>For performance, only one merge adapter will be initialized within this application's context.
 *
 * <p>Note: The ConfigSyncService itself is not notified when a new merge occurs. It's role is to
 * manage the lifecycle of our {@link DataSyncAdapter} and provide a handle to said DataSyncAdapter to the
 * OS on request.
 */
public class DataSyncService extends IntentService {
    private static final String TAG = "DataSyncService";

    private static final Object sSyncAdapterLock = new Object();
    private static DataSyncAdapter sDataSyncAdapter = null;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public DataSyncService() {
        super("data_sync_service");
    }

    /**
     * Thread-safe constructor, creates static {@link DataSyncAdapter} instance.
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
     * Return Binder handle for IPC communication with {@link DataSyncAdapter}.
     *
     * <p>New merge requests will be sent directly to the DataSyncAdapter using this channel.
     *
     * @param intent Calling intent
     * @return Binder handle for {@link DataSyncAdapter}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return sDataSyncAdapter.getSyncAdapterBinder();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent() called with intent: " + intent);
    }
}
