package com.timappweb.timapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.BuildConfig;
import com.timappweb.timapp.MyApplication;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatterBuilder;
import org.ocpsoft.prettytime.PrettyTime;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import eu.davidea.flexibleadapter.utils.DrawableUtils;

public class Util {

    private static long currentTimeMilli;

    public static String print(Location location) {
        return location.getLongitude()+"-"+location.getLatitude()+ " (Accuracy"+location.getAccuracy()+")";
    }

    public static int getCurrentTimeSec() {
        return (int)(System.currentTimeMillis() / 1000);
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

    //Get Pretty time from TimeStamp
    public static String millisTimestampToPrettyTime(long created) {
        PrettyTime p = new PrettyTime();
        String pFormated = p.format(new Date(created));
        return Util.capitalize(pFormated);
    }
    public static String secondsTimestampToPrettyTime(long created) {
        return millisTimestampToPrettyTime(created*1000);
    }

    //Get PrettyTime From duration
    public static String secondsDurationToPrettyTime(int duration) {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder()
                .appendDays()
                .appendSuffix(" day", " days")
                .appendSeparator(" ")
                .appendHours()
                .appendSuffix(" hour", " hours");
        if(duration<86400) {
            builder .appendSeparator(" and ")
                    .appendMinutes()
                    .appendSuffix(" min", " mins");
        }
        if (duration < 60) {
            builder .appendSeparator(" and ")
                    .appendSeconds()
                    .appendSuffix(" sec", " secs");
        }
        Period period =  new Period(duration * 1000);
        return builder.toFormatter().print(period.normalizedStandard());
    }
    public static String millisDurationToPrettyTime(int duration) {
        return secondsDurationToPrettyTime(duration/1000);
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

    public static void appStateError(final String TAG, String msg) {
        if (BuildConfig.DEBUG){
            throw new InternalError(msg);
        }
        else{
            Log.e(TAG, msg);
        }
    }

    public static Bitmap getBmpFromImgView(ImageView imageView, int iconDiameter) {
        Bitmap bmp = Bitmap.createScaledBitmap(imageView.getDrawingCache(),
                iconDiameter,iconDiameter,true);
        imageView.setDrawingCacheEnabled(false); // clear drawing cache

        bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
        return bmp;
    }

    public static LatLng roundLatLng(LatLng initialLatLng) {
        DecimalFormat df = new DecimalFormat("#.#####");
        df.setRoundingMode(RoundingMode.CEILING);
        double roundedLatitude = Double.parseDouble(df.format(initialLatLng.latitude));;
        double roundedLongitude = Double.parseDouble(df.format(initialLatLng.longitude));
        return new LatLng(roundedLatitude,roundedLongitude);
    }

    public static void appAssert(final boolean b, final String TAG, final String msg) {
        if (!b){
            appStateError(TAG, msg);
        }
    }

    public static float dpToPx(float dp, Context context) {
        return com.github.florent37.materialviewpager.Utils.dpToPx(dp, context);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) MyApplication.getApplicationBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    public static boolean isOlderThan(long date, long duration) {
        return (System.currentTimeMillis()-date) > duration;
    }

    public static void changeColorActionBar(AppCompatActivity activity, int color) {
        ActionBar bar = activity.getSupportActionBar();
        if(bar!=null) {
            bar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(activity, color)));
        }
    }

    public static void setSelectionsBackgroundAdapter(View itemView, int unpressedColor, int rippleColor, int pressedColor) {
        Context context = MyApplication.getApplicationBaseContext();

        DrawableUtils.setBackground(itemView,
                DrawableUtils.getSelectableBackgroundCompat(
                        ContextCompat.getColor(context, rippleColor),
                        ContextCompat.getColor(context, unpressedColor),
                        ContextCompat.getColor(context, pressedColor)));
    }

    public static void setStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(activity, color));
        }
    }
}
