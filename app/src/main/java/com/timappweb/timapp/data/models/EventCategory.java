package com.timappweb.timapp.data.models;

import android.util.Log;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.DrawableUtil;

import java.io.Serializable;
import java.util.List;

@Table(name = "EventCategory")
public class EventCategory extends SyncBaseModel{

    private static final String TAG = "EventCategory";

    // =============================================================================================

    @Expose
    @Column(name = "Name")
    public String name;

    @Expose
    @Column(name = "Position")
    public int position;

    // =============================================================================================


    public EventCategory() {
    }

    public EventCategory(String name) {
        this.name = name;
        this.position = 0;
    }
    public EventCategory(String name, int position) {
        this.name = name;
        this.position = position;
    }

    // =============================================================================================
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
        String res = String.valueOf(categories.get(0).remote_id);

        for (int i = 1; i < categories.size(); i++){
            res += "," + String.valueOf(categories.get(i).remote_id);
        }
        return res;
    }


    public int getSmallIcon() {
        return getSmallIcon(this.name);
    }

    public static int getSmallIcon(String name) {
        try {
            return DrawableUtil.get("ic_category_" + name);
        } catch (DrawableUtil.UnknownDrawableException e) {
            Log.e(TAG, "Unknown category drawable for " + name);
            return R.drawable.ic_category_unknown;
        }
    }

    public int getBigIcon() {
        try {
            return DrawableUtil.get("image_" + this.name);
        } catch (DrawableUtil.UnknownDrawableException e) {
            Log.e(TAG, "Unknown category drawable for " + this.name);
            return R.drawable.image_else;
        }
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
                ", id=" + remote_id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventCategory)) return false;
        if (!super.equals(o)) return false;

        EventCategory that = (EventCategory) o;

        if (remote_id != that.remote_id) return false;
        if (position != that.position) return false;
        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + remote_id;
        return result;
    }

}
