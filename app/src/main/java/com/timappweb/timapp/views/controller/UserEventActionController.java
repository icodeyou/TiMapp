package com.timappweb.timapp.views.controller;

import android.location.Location;

import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.utils.location.LocationManager;

/**
 * Created by Stephane on 08/08/2016.
 */

public class UserEventActionController{

    public static void monitor(final Event event, final Listener listener) {

        LocationManager.addOnLocationChangedListener(new LocationManager.LocationListener() {
            @Override
            public void onLocationChanged(Location newLocation, Location lastLocation) {
                if (event.isUserAround()){
                    if (!event.isUserAround(lastLocation.getLatitude(), lastLocation.getLongitude())){
                        listener.onUserAround();
                    }
                }
                else if (event.isUserAround(lastLocation.getLatitude(), lastLocation.getLongitude())){
                    listener.onUserLeft();
                }
            }
        });
    }


    public interface Listener{

        void onUserLeft();

        void onUserAround();

    }

}