package com.timappweb.timapp.data.loader;

import android.content.Context;
import android.location.Location;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.data.models.SyncBaseModel;
import com.timappweb.timapp.data.models.SyncHistoryBounds;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.utils.location.LocationManager;


/**
 * Created by Stephane on 11/08/2016.
 */

public class MapAreaLoaderCallback<T extends SyncBaseModel> extends MultipleEntryLoaderCallback<T> {

    /**
     * Margin when loading place max reachable around user (in meters)
     */
    private static final String         TAG                             = "MapAreaLoaderCallback";

    private LatLngBounds                bounds;
    private int                         expandSize = 0;

    // ---------------------------------------------------------------------------------------------

    public MapAreaLoaderCallback(Context context, int syncType, Class<T> clazz) {
        super(context, 0, syncType, clazz);
    }

    public void setBounds(LatLngBounds bounds){
        this.query = SyncBaseModel.queryByArea(bounds, clazz);
        this.bounds = bounds;
    }

    public void setBounds(Location location, int size) {
        LatLngBounds bounds = LocationManager.generateBoundsAroundLocation(
                location,
                size
        );
        this.setBounds(bounds);
    }

    public void setExpandSize(int expandSize) {
        this.expandSize = expandSize;
    }

    public boolean hasBounds() {
        return bounds != null;
    }

    @Override
    public void fetchEntries(boolean force) {
        if (force || SyncHistoryBounds.requireUpdate(this.getSyncType(), bounds, this.getSyncDelay())){
            LatLngBounds newBounds = LocationManager.expand(bounds, expandSize);
            this.syncOption.set(DataSyncAdapter.SYNC_PARAM_MAP_BOUNDS, newBounds);
            Log.d(TAG, "Require update for bounds: " + bounds);
            Log.d(TAG, "    - Loading expanded bounds: " + newBounds);
            SyncBaseModel.getRemoteEntries(this.context, this.syncOption);
        }
        else{
            Log.i(TAG, "Entries are already in db. Not update required");
        }
    }

}