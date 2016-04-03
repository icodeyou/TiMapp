package com.timappweb.timapp.entities;

import android.util.Log;

import com.timappweb.timapp.R;

import java.io.Serializable;
import java.util.List;

public class Category implements Serializable{

    private static final String TAG = "Category";
    public int id;
    public String name;

    private int resourceBlack = -1;
    private int resourceWhite = -1;
    private int layoutResId = -1;
    private int smallImageResId = -1;
    private int bigImageResId= -1;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        return name.equals(category.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }


    public static String idsToString(List<Category> categories) {
        if (categories == null || categories.size() == 0){
            return "";
        }
        String res = String.valueOf(categories.get(0).id);

        for (int i = 1; i < categories.size(); i++){
            res += "," + String.valueOf(categories.get(i).id);
        }
        return res;
    }
    public int getIconWhiteResId() {
        if (resourceWhite == -1){
            try {
                resourceWhite = R.drawable.class.getField("ic_category_highlight_" + this.name).getInt(null);
                Log.v(TAG, "Resource read from field ic_category_" + this.name + ": " + resourceWhite);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Unknown category drawable for " + this.name);
                resourceWhite = R.drawable.ic_category_highlight_unknown;
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "Unknown category drawable for " + this.name);
                resourceWhite = R.drawable.ic_category_highlight_unknown;
            }
        }
        Log.v(TAG, "Getting icon resource: " + resourceWhite);
        return resourceWhite;
    }

    public int getIconBlackResId() {
        if (resourceBlack == -1){
            try {
                resourceBlack = R.drawable.class.getField("ic_category_" + this.name).getInt(null);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Unknown category drawable for " + this.name);
                resourceBlack = R.drawable.ic_category_unknown;
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "Unknown category drawable for " + this.name);
                resourceBlack = R.drawable.ic_category_unknown;
            }
        }
        return resourceBlack;
    }


    public int getLayoutResId() {
        if (layoutResId != -1){
            return layoutResId;
        }
        try {
            layoutResId = R.layout.class.getField("category_" + this.name).getInt(null);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Unknown category layout for " + this.name);
            layoutResId = R.layout.category_unknown;
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Unknown category layout for " + this.name);
            layoutResId = R.layout.category_unknown;
        }
        return layoutResId;
    }

    public int getBigImageResId() {
        if (bigImageResId != -1){
            return bigImageResId;
        }
        try {
            bigImageResId = R.drawable.class.getField("image_" + this.name).getInt(null);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Unknown category layout for " + this.name);
            bigImageResId = R.drawable.image_unknown;
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Unknown category layout for " + this.name);
            bigImageResId = R.drawable.image_unknown;
        }
        return bigImageResId;
    }

    public int getSmallImageResId() {
        if (smallImageResId != -1){
            return smallImageResId;
        }
        try {
            smallImageResId = R.drawable.class.getField("image_place_" + this.name).getInt(null);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Unknown category layout for " + this.name);
            smallImageResId = R.drawable.image_place_unknown;
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Unknown category layout for " + this.name);
            smallImageResId = R.drawable.image_place_unknown;
        }
        return smallImageResId;
    }

    public int getTitleResId() {
        try {
            return R.string.class.getField("category_" + this.name).getInt(null);
        } catch (IllegalAccessException e) {
        } catch (NoSuchFieldException e) {
        }
        Log.e(TAG, "Unknown category title for " + this.name);
        return R.string.category_unknown;
    }
}
