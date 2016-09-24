package com.timappweb.timapp.utils.mocklocations;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Stephane on 23/09/2016.
 */
public abstract class AbstractMockLocationProvider {

    private static final String TAG = "MockLocationProvider";

    public abstract Location pushLocation(LatLng ll);
    public abstract Location pushLocation(Location loc);

    public static Location createMockLocation(String providerName, double lat, double lon){
        Location mockLocation = new Location(providerName);
        mockLocation.setLatitude(lat);
        mockLocation.setLongitude(lon);
        mockLocation.setAltitude(0);
        mockLocation.setAccuracy(1);
        mockLocation.setSpeed(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mockLocation.setElapsedRealtimeNanos(System.currentTimeMillis() * 1000);
        }
        mockLocation.setTime(System.currentTimeMillis());
        return mockLocation;
    }

    public void route(final MockLocationRoute route, final long delay){
        /*
        final Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                Location loc = route.getNextLocation();
                Log.i(TAG, "GENERATING NEW MOCK LOCATION: " + loc);
                if(loc != null) {
                    AbstractMockLocationProvider.this.pushLocation(new LatLng(loc.getLatitude(), loc.getLongitude()));
                    handler.postDelayed(this, delay);
                }
            }
        };
        handler.postDelayed(r, delay);*/
    }

    public interface MockLocationRoute {
        Location getNextLocation();
    }


}
