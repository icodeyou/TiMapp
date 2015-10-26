package com.timappweb.timapp.utils;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by stephane on 9/10/2015.
 */
public abstract class MyLocationListener implements LocationListener{

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
