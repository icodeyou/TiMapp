package com.timappweb.timapp.utils;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.util.DisplayMetrics;
import android.webkit.MimeTypeMap;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by stephane on 9/10/2015.
 */
public class Util {

    private static PeriodFormatter _daysHoursMinutesFormater = null;
    private static long currentTimeMilli;

    public static String print(Location location) {
        return location.getLongitude()+"-"+location.getLatitude()+ " (Accuracy"+location.getAccuracy()+")";
    }

    public static int getCurrentTimeSec() {
        return (int)(System.currentTimeMillis() / 1000);
    }

    /**
     * True if date is at least duration old
     * @param date seconds
     * @param duration seconds
     * @return
     */
    public static boolean isOlderThan(int date, int duration) {
        return (getCurrentTimeSec()-date) > duration;
    }


    public static int delayFromNow(int date) {
        return Util.getCurrentTimeSec() - date;
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

    public static String secondsTimestampToPrettyTime(int created) {
        PrettyTime p = new PrettyTime();
        return p.format(new Date((long) created * 1000));
    }
    public static String secondsDurationToPrettyTime(int duration) {
        // TODO define
        if (_daysHoursMinutesFormater == null){
            _daysHoursMinutesFormater = new PeriodFormatterBuilder()
                    .appendDays()
                    .appendSuffix(" day", " days")
                    .appendSeparator(" and ")
                    .appendMinutes()
                    .appendSuffix(" min", " mins")
                    .appendSeparator(" and ")
                            //.appendSeconds()
                            //.appendSuffix(" sec", " secs")
                    .toFormatter();
        }
        Period period =  new Period(duration * 1000);
        //System.out.println(daysHoursMinutes.print(period));
        return _daysHoursMinutesFormater.print(period.normalizedStandard());
    }

    public static String byteToKB(long size) {
        return ((double)size / (1000.0*1000)) + "MB";
    }

    public static boolean isSameDate(Calendar A, Calendar B, int type){
        A.set(type, 0);
        B.set(type, 0);
        return A.getTimeInMillis() == B.getTimeInMillis();
    }

    public static String capitalize(String string) {
        return string.substring(0,1).toUpperCase() + string.substring(1);
    }

}
