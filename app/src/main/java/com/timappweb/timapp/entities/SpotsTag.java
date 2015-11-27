package com.timappweb.timapp.entities;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class SpotsTag implements ClusterItem {

    public static final SpotsTag[] ITEMS = {
            new SpotsTag(-30, 23, 23),
            new SpotsTag(-33, 26, 13),
            new SpotsTag(-26, 30, 13),
    } ;

    public SpotsTag(double latitude, double longitude, int count_ref) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.count_ref = count_ref;
    }

    public LatLng getLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }

    public double latitude;
    public double longitude;
    public int count_ref;
    public String name;

    @Override
    public LatLng getPosition() {
        return this.getLatLng();
    }
}
