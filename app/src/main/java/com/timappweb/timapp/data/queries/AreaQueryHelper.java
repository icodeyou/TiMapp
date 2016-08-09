package com.timappweb.timapp.data.queries;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by stephane on 6/2/2016.
 *
 * SQL query helper for areas
 * Implementation is base on Android implementation LatLngBounds::contain()
 *
 */
public class AreaQueryHelper {


    /**
     * Create a condition to get all locations that are in the area bounds
     *
     *  -           -
     *       ________________________
     *      |                        |
     *      |  X         X           |
     *      |                        |     -
     *      |      X                 |
     *      |                 X      |
     *      |________________________|
     *                  - -
     *      -
     *
     * @param bounds
     * @return
     */
    public static String rowInBounds(LatLngBounds bounds){
        return zzi(bounds) + " AND " + zzj(bounds);
    }

    // =============================================================================================

    private static String zzi(LatLngBounds bounds) {
        return "(Latitude >= " + bounds.southwest.latitude + " AND Latitude <= " + bounds.northeast.latitude + ")";
    }

    private static String zzj(LatLngBounds bounds) {
        return bounds.southwest.longitude <= bounds.northeast.longitude
                ? "(Longitude >= " + bounds.southwest.longitude + " AND Longitude <= " + bounds.northeast.longitude + ")"
                : "(Longitude >= " + bounds.southwest.longitude + " OR Longitude <= " + bounds.northeast.longitude + ")";
    }
}
