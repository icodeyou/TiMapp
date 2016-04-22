package com.timappweb.timapp.entities;

import android.util.Log;

import com.timappweb.timapp.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SpotCategory implements Serializable{

    private static final String TAG = "SpotCategory";
    public int id;
    public String name;
    public String resource;

    private int resourceWhite;


    public SpotCategory(String name) {
        this.name = name;
    }

    public static SpotCategory createDummy() {
        return new SpotCategory("DummyCategory");
    }

    /*public int getIconWhiteResId() {
        if (resourceWhite == -1){
            try {
                resourceWhite = R.drawable.class.getField("spot_category_" + this.name).getInt(null);
                Log.v(TAG, "Resource read from field ic_category_" + this.name + ": " + resourceWhite);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Unknown category drawable for " + this.name);
                resourceWhite = R.drawable.ic_category_highlight_else;
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "Unknown category drawable for " + this.name);
                resourceWhite = R.drawable.ic_category_highlight_else;
            }
        }
        Log.v(TAG, "Getting icon resource: " + resourceWhite);
        return resourceWhite;
    }*/
}
