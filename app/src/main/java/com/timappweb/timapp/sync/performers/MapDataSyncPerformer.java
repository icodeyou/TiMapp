package com.timappweb.timapp.sync.performers;

import android.content.SyncResult;
import android.location.Location;

import com.activeandroid.query.From;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.MapAreaInfo;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.model.PaginationResponse;

/**
 * Created by stephane on 5/6/2016.
 */
public class MapDataSyncPerformer implements SyncPerformer {

    @Override
    public void perform() {

    }

    private void performMapDataSync(LatLngBounds bounds, PaginationResponse<? extends SyncBaseModel> response, SyncResult syncResult){

        if (MyApplication.hasFineLocation()){
            Location location = MyApplication.getLastLocation();
            // TODO
            From localQuery = MapAreaInfo.findArea(null, MapAreaInfo.AROUND_USER);
            /*
            this.performModelSync(Place.class,
                    RestClient.service().placeReachable(location.getLatitude(), location.getLongitude()),
                    localQuery,
                    syncResult);*/
        }

        MapAreaInfo.addNewArea(bounds, MapAreaInfo.MAP_EVENT, response.total, response.items.size());
        for (SyncBaseModel model: response.items){

        }

    }
}
