package com.timappweb.timapp.utils;

import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by stephane on 9/10/2015.
 */
public abstract class MyLocationListener implements LocationListener{

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(MyLocationProvider.TAG, "Status changed for " + provider + ": " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(MyLocationProvider.TAG, "Provider " + provider + " is now enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(MyLocationProvider.TAG, "Provider " + provider + " is now disabled");
    }
}
