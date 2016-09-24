package com.timappweb.timapp.utils.mocklocations;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Stephane on 08/09/2016.
 *
 * From: https://mobiarch.wordpress.com/2012/07/17/testing-with-mock-location-data-in-android/
 */
public class MockLocationProvider extends AbstractMockLocationProvider {
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

    @Override
    public Location pushLocation(LatLng ll) {
        Location mockLocation = createMockLocation(providerName, ll.latitude, ll.longitude);
        return this.pushLocation(mockLocation);
    }

    @Override
    public Location pushLocation(Location loc) {
        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);

        lm.setTestProviderLocation(providerName, loc);
        return loc;
    }

    public void shutdown() {
        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);
        lm.removeTestProvider(providerName);
    }

    // ---------------------------------------------------------------------------------------------

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
