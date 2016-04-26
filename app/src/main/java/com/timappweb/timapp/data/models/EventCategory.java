package com.timappweb.timapp.data.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.R;

import java.io.Serializable;
import java.util.List;

@Table(name = "EventCategory")
public class EventCategory extends SyncBaseModel implements Serializable{

    private static final String TAG = "EventCategory";

    @Expose
    @Column(name = "SyncId")
    public int id;

    @Expose
    @Column(name = "Name")
    public String name;

    @Expose
    @Column(name = "Position")
    public int position;


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


    public static String idsToString(List<EventCategory> categories) {
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
                resourceWhite = R.drawable.ic_category_highlight_else;
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "Unknown category drawable for " + this.name);
                resourceWhite = R.drawable.ic_category_highlight_else;
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
                resourceBlack = R.drawable.ic_category_else;
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "Unknown category drawable for " + this.name);
                resourceBlack = R.drawable.ic_category_else;
            }
        }
        return resourceBlack;
    }

    public int getBigImageResId() {
        if (bigImageResId != -1){
            return bigImageResId;
        }
        try {
            bigImageResId = R.drawable.class.getField("image_" + this.name).getInt(null);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Unknown category layout for " + this.name);
            bigImageResId = R.drawable.image_else;
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Unknown category layout for " + this.name);
            bigImageResId = R.drawable.image_else;
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
            smallImageResId = R.drawable.image_place_else;
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Unknown category layout for " + this.name);
            smallImageResId = R.drawable.image_place_else;
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

    @Override
    public long getSyncKey() {
        return this.id;
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        if (!(model instanceof EventCategory)) return false;
        EventCategory that = (EventCategory) model;

        if (position != that.position) return false;
        return name.equals(that.name);
    }

    @Override
    public String toString() {
        return "EventCategory{" +
                "position=" + position +
                ", name='" + name + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventCategory)) return false;
        if (!super.equals(o)) return false;

        EventCategory that = (EventCategory) o;

        if (id != that.id) return false;
        if (position != that.position) return false;
        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id;
        return result;
    }
}
