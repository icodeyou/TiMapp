package com.timappweb.timapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.SystemClock;

/**
 * Created by Stephane on 07/09/2016.
 */
public class TestUtil {

    public static void sleep(long time) {
        SystemClock.sleep(time);
    }

    public static boolean hasNetworkAccess(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
