package com.timappweb.timapp.utils;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Stephane on 08/09/2016.
 *
 * From: https://mobiarch.wordpress.com/2012/07/17/testing-with-mock-location-data-in-android/
 */
public class MockLocationProvider {
    String providerName;
    Context ctx;

    private MockLocationProvider(String name, Context ctx) {
        this.providerName = name;
        this.ctx = ctx;

        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);
        lm.addTestProvider(providerName, false, false, false, false, false,
                true, true, 0, 5);
        lm.setTestProviderEnabled(providerName, true);
    }

    public Location pushLocation(LatLng ll) {
        return this.pushLocation(ll.latitude, ll.longitude);
    }

    public Location pushLocation(double lat, double lon) {
        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);

        Location mockLocation = createMockLocation(providerName, lat, lon);
        lm.setTestProviderLocation(providerName, mockLocation);
        return mockLocation;
    }

    public void shutdown() {
        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);
        lm.removeTestProvider(providerName);
    }

    // ---------------------------------------------------------------------------------------------

    public static Location createMockLocation(String providerName, double lat, double lon){
        Location mockLocation = new Location(providerName);
        mockLocation.setLatitude(lat);
        mockLocation.setLongitude(lon);
        mockLocation.setAltitude(0);
        mockLocation.setAccuracy(10);
        mockLocation.setSpeed(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mockLocation.setElapsedRealtimeNanos(System.currentTimeMillis() * 1000);
        }
        mockLocation.setTime(System.currentTimeMillis());
        return mockLocation;
    }

    public static MockLocationProvider createGPSProvider(Activity activity){
        return new MockLocationProvider(LocationManager.GPS_PROVIDER, activity);
    }

    public static MockLocationProvider createNetworkProvider(Activity activity){
        return new MockLocationProvider(android.location.LocationManager.NETWORK_PROVIDER, activity);
    }


    // ---------------------------------------------------------------------------------------------

    public class MockLocationProviderFixture{

    }
}
