package com.timappweb.timapp.utils;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by stephane on 9/17/2015.
 */
public class IntLatLng {
    public static final int precision = 10000; // Precision = 5 number

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
        this.latitude = (int) (ll.latitude * precision);
        this.longitude = (int) (ll.longitude * precision);
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
        return new LatLng((double)this.latitude / precision, (double)this.longitude / precision);
    }
}
