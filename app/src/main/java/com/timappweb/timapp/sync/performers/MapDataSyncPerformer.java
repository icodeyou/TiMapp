package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;
import android.location.Location;

import com.activeandroid.query.From;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.data.models.MapAreaInfo;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.rest.model.PaginatedResponse;
import com.timappweb.timapp.utils.location.LocationManager;

/**
 * Created by stephane on 5/6/2016.
 */
public class MapDataSyncPerformer implements SyncPerformer {

    @Override
    public void perform() {

    }

    private void performMapDataSync(LatLngBounds bounds, PaginatedResponse<? extends SyncBaseModel> response, SyncResult syncResult){

        if (LocationManager.hasFineLocation()){
            Location location = LocationManager.getLastLocation();
            // TODO
            From localQuery = MapAreaInfo.findArea(null, MapAreaInfo.AROUND_USER);
            /*
            this.performModelSync(Event.class,
                    RestClient.service().placeReachable(location.getLatitude(), location.getLongitude()),
                    localQuery,
                    syncResult);*/
        }

        MapAreaInfo.addNewArea(bounds, MapAreaInfo.MAP_EVENT, response.total, response.items.size());
        for (SyncBaseModel model: response.items){

        }

    }
}
