package com.timappweb.timapp.services;

import com.activeandroid.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.timappweb.timapp.MyApplication;

/**
 * Created by stephane on 4/3/2016.
 */
public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = "MyInstanceIDListenerService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        if (MyApplication.isLoggedIn()){
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            Log.i(TAG, "Refreshing GCM token: " + refreshedToken);
            MyApplication.updateGoogleMessagingToken(getApplicationContext(), refreshedToken);
        }
    }

}
