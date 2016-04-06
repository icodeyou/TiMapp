package com.timappweb.timapp.utils;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.util.DisplayMetrics;
import android.webkit.MimeTypeMap;

import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

/**
 * Created by stephane on 9/10/2015.
 */
public class Util {

    public static String print(Location location) {
        return location.getLongitude()+"-"+location.getLatitude()+ " (Accuracy"+location.getAccuracy()+")";
    }

    public static int getCurrentTimeSec() {
        return (int)(System.currentTimeMillis() / 1000);
    }

    /**
     *
     * @param created seconds
     * @param old seconds
     * @return
     */
    public static boolean isOlderThan(int created, int old) {
        return (getCurrentTimeSec()-created) > old;
    }


    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp NewActivity value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return NewActivity float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px NewActivity value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return NewActivity float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String secondsTimestampToPrettyTime(long created) {
        PrettyTime p = new PrettyTime();
        return p.format(new Date(created));
    }

    public static String byteToKB(long size) {
        return ((double)size / (1000.0*1000)) + "MB";
    }
}
