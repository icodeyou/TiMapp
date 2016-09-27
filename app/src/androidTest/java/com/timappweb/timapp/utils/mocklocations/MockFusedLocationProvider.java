package com.timappweb.timapp.utils.mocklocations;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.location.MyLocationProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Stephane on 08/09/2016.
 *
 * From: https://mobiarch.wordpress.com/2012/07/17/testing-with-mock-location-data-in-android/
 */
public class MockFusedLocationProvider extends AbstractMockLocationProvider {

    private static final String TAG = "MockFusedLocationProvid";

    private final GoogleApiClient mGoogleApiClient;
    private final LinkedList<Location> pendingLocations;

    private MockFusedLocationProvider(GoogleApiClient googleApiClient) {
        this.mGoogleApiClient = googleApiClient;
        pendingLocations = new LinkedList<>();
        if (this.mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
        }
        LocationManager.getLocationProvider().setConnectionCallback(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                Log.w(TAG, "onConnected() SETTING MOCK MODE FOR LOCATION PROVIDER");
                LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
                for (Location location: pendingLocations){
                    pushLocation(location);
                }
                pendingLocations.clear();
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        });
    }

    public static MockFusedLocationProvider create(final GoogleApiClient googleApiClient) {
        return new MockFusedLocationProvider(googleApiClient);
    }

    @Override
    public Location pushLocation(LatLng ll) {
        Location location = createMockLocation("MockedLocation", ll.latitude, ll.longitude);
        return this.pushLocation(location);
    }

    @Override
    public Location pushLocation(Location loc) {
        if (this.mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.setMockLocation(this.mGoogleApiClient, loc);
        }
        else{
            pendingLocations.add(loc);
        }
        return loc;
    }


}
