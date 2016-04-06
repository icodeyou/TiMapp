package com.timappweb.timapp.receivers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GcmReceiver;
import com.timappweb.timapp.services.MyGcmListenerService;

/**
 * Created by stephane on 4/3/2016.
 */
public class MyGcmReceiver extends GcmReceiver {

    private static final String TAG = "MyGcmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "GCM RECEVEID: " + intent);
        super.onReceive(context, intent);

        Log.d(TAG, "GCM resultCode= " + this.getResultCode());
    }

}
