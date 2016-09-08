package com.timappweb.timapp.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;

/**
 * Created by Stephane on 08/09/2016.
 *
 * From: https://mobiarch.wordpress.com/2012/07/17/testing-with-mock-location-data-in-android/
 */
public class MockLocationProvider {
    String providerName;
    Context ctx;

    public MockLocationProvider(String name, Context ctx) {
        this.providerName = name;
        this.ctx = ctx;

        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);
        lm.addTestProvider(providerName, false, false, false, false, false,
                true, true, 0, 5);
        lm.setTestProviderEnabled(providerName, true);
    }

    public Location pushLocation(double lat, double lon) {
        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);

        Location mockLocation = create(providerName, lat, lon);
        lm.setTestProviderLocation(providerName, mockLocation);
        return mockLocation;
    }

    public void shutdown() {
        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);
        lm.removeTestProvider(providerName);
    }

    public static Location create(String providerName, double lat, double lon){
        Location mockLocation = new Location(providerName);
        mockLocation.setLatitude(lat);
        mockLocation.setLongitude(lon);
        mockLocation.setAltitude(0);
        mockLocation.setTime(System.currentTimeMillis());
        return mockLocation;
    }
}
