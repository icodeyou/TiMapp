package com.timappweb.timapp.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.maps.android.clustering.*;
import com.timappweb.timapp.entities.Spot;

/**
 * Created by stephane on 9/12/2015.
 * TODO REMOVE
 */
public class OnCameraChangeListener implements GoogleMap.OnCameraChangeListener {

    private ClusterManager mClusterManagerSpot;

    public OnCameraChangeListener(ClusterManager clusterManagerSpot) {
        this.mClusterManagerSpot = clusterManagerSpot;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        this.mClusterManagerSpot.onCameraChange(cameraPosition);

        // Update data

    }
}
