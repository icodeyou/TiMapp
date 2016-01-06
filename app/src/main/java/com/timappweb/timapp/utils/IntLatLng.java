package com.timappweb.timapp.utils;

import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.utils.AreaDataCaching.CoordinateConverter;

/**
 * Created by stephane on 9/17/2015.
 */
public class IntLatLng {

    public int latitude;
    public int longitude;

    @Override
    public String toString() {
        return "IntLatLng{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public IntLatLng(int latitude, int longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public IntLatLng(LatLng ll) {
        this.latitude = CoordinateConverter.convert(ll.latitude);
        this.longitude = CoordinateConverter.convert(ll.longitude);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntLatLng intLatLng = (IntLatLng) o;

        if (latitude != intLatLng.latitude) return false;
        return longitude == intLatLng.longitude;

    }

    @Override
    public int hashCode() {
        int result = latitude;
        result = 31 * result + longitude;
        return result;
    }

    public LatLng toDouble() {
        return new LatLng(CoordinateConverter.convert(this.latitude), CoordinateConverter.convert(this.longitude));
    }
}
