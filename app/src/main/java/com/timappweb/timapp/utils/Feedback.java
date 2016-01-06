package com.timappweb.timapp.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by stephane on 9/12/2015.
 */
public class Feedback {

    public static void show(Context c, String m){
        Toast.makeText(c, m, Toast.LENGTH_LONG);
    }

    public static void show(Context c, int r){
        Toast.makeText(c, c.getResources().getString(r), Toast.LENGTH_LONG);
    }

    public static void error(Context applicationContext, String s) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_LONG);
    }
}
