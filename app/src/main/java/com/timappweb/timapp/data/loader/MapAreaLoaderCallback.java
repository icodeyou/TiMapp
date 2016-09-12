package com.timappweb.timapp.data.loader;

import android.content.Context;
import android.location.Location;
import android.support.v4.content.Loader;
import android.util.Log;

import com.activeandroid.query.From;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.data.loader.paginate.PaginateDataLoader;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.utils.location.LocationManager;

import java.util.List;


/**
 * Created by Stephane on 11/08/2016.
 */

public class MapAreaLoaderCallback<T extends SyncBaseModel> extends PaginateDataLoader<T> {

    private static final String         TAG                             = "MapAreaLoaderCallback";

    // ---------------------------------------------------------------------------------------------

    public MapAreaLoaderCallback() {
        super();
    }


}