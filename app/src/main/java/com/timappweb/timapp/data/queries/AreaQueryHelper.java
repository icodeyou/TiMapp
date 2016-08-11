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
     * Create a condition to get all locations that are in the area bounds given in parameter
     * Based on android implementation LatLngBounds.contain()
     *
     * @param bounds
     * @return
     */
    public static String rowInBounds(LatLngBounds bounds){
        return rowInBounds(bounds, "Latitude", "Longitude");
    }
    public static String rowInBounds(LatLngBounds bounds, String latitudeFields, String longitudeField){
        return "("+zzi(bounds, latitudeFields) + " AND " + zzj(bounds, longitudeField)+")";
    }

    // =============================================================================================

    /**

     * @param bounds
     * @param latitudeFields
     * @return
     */
    private static String zzi(LatLngBounds bounds, String latitudeFields) {
        return "("+latitudeFields+" >= " + bounds.southwest.latitude + " AND "+latitudeFields+" <= " + bounds.northeast.latitude + ")";
    }

    private static String zzj(LatLngBounds bounds, String longitudeField) {
        return bounds.southwest.longitude <= bounds.northeast.longitude
                ? "("+longitudeField+" >= " + bounds.southwest.longitude + " AND "+longitudeField+" <= " + bounds.northeast.longitude + ")"
                : "("+longitudeField+" >= " + bounds.southwest.longitude + " OR "+longitudeField+" <= " + bounds.northeast.longitude + ")";
    }

    public static String locationInSQLBounds(double latitude, double longitude) {
        return String.format("((%1$f >= LatitudeSW AND %1$f <= LatitudeNE) AND " +
                "( " +
                "   (%2$f >= LongitudeSW AND %2$f <= LongitudeNE AND LongitudeSW <= LongitudeNE) " +
                "   OR " +
                "   ((%2$f >= LongitudeSW OR %2$f <= LongitudeNE) AND LongitudeSW > LongitudeNE)" +
                "))", latitude, longitude);
    }
}
