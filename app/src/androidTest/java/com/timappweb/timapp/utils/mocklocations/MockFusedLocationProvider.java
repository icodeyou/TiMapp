package com.timappweb.timapp.utils.mocklocations;

import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Stephane on 08/09/2016.
 *
 * From: https://mobiarch.wordpress.com/2012/07/17/testing-with-mock-location-data-in-android/
 */
public class MockFusedLocationProvider extends AbstractMockLocationProvider {

    private final GoogleApiClient mGoogleApiClient;

    private MockFusedLocationProvider(GoogleApiClient googleApiClient) {
        this.mGoogleApiClient = googleApiClient;
        LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
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
        LocationServices.FusedLocationApi.setMockLocation(this.mGoogleApiClient, loc);
        return loc;
    }


}
