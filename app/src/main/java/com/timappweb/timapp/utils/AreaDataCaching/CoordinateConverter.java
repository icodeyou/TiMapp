package com.timappweb.timapp.utils.AreaDataCaching;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.utils.IntLatLng;
import com.timappweb.timapp.utils.IntLatLngBounds;

/**
 * Created by stephane on 12/9/2015.
 */
public class CoordinateConverter {

    public static final int precision = 10000; // Precision = 5 number

    public static int convert(double value){
        return (int) (value * precision);
    }
    public static double convert(int value){
        return ((double)value / precision);
    }

    public static LatLngBounds convert(IntLatLngBounds bounds) {
        return new LatLngBounds(CoordinateConverter.convert(bounds.southwest), CoordinateConverter.convert(bounds.northeast));
    }

    private static LatLng convert(IntLatLng p) {
        return new LatLng(CoordinateConverter.convert(p.latitude), CoordinateConverter.convert(p.longitude));
    }
}
