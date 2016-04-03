package com.timappweb.timapp.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;

import java.io.IOException;

/**
 * Created by stephane on 4/3/2016.
 */
public class RegistrationIntentService extends IntentService{

    private static final String TAG = "RegistrationIntent";

    public RegistrationIntentService(){
        super("GCMRegistrationIntent");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            Log.v(TAG, "::onHandleIntent()");
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            MyApplication.updateGoogleMessagingToken(token);
        } catch (IOException e) {
            Log.e(TAG, "Cannot get a new GCM token");
            e.printStackTrace();
        }
    }

}
