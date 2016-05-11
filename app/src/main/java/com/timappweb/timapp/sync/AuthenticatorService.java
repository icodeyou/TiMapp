package com.timappweb.timapp.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * The service which allows the merge adapter framework to access the authenticator.
 */
public class AuthenticatorService extends Service {
    private static final String TAG = "AuthenticatorService";
    // Instance field that stores the authenticator object
    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        Log.d(TAG, "Creating AuthenticatorService");
        // Create a new authenticator object
        mAuthenticator = new Authenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
