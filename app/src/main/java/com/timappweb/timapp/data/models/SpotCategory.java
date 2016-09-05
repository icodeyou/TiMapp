package com.timappweb.timapp.data.models;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.PictureUtility;

/**
 * Created by stephane on 4/5/2016.
 */
@Table(name = "SpotCategory")
public class SpotCategory extends Category{

    // =============================================================================================

    @Expose
    @SerializedName("name")
    @Column(name = "Name")
    public String name;

    @Expose
    @SerializedName("position")
    @Column(name = "Position")
    public int position;

    @Expose
    @SerializedName("icon")
    @Column(name = "ResourceName")
    public String iconUrl;

    // =============================================================================================

    // Used as cache
    private Drawable _iconDrawable;

    // =============================================================================================

    public SpotCategory() {
        super();
    }

    public SpotCategory(String name) {
        super();
        this.name = name;
    }

    // =============================================================================================
    // SyncBaseModel overides

    @Override
    public boolean isSync(SyncBaseModel model) {
        if (!(model instanceof SpotCategory)) return false;
        SpotCategory that = (SpotCategory) model;

        if (position != that.position) return false;
        return name.equals(that.name);
    }

    // =============================================================================================

    @Override
    public String toString() {
        return "SpotCategory{" +
                "remote_id=" + remote_id +
                ", name='" + name + '\'' +
                ", position=" + position +
                '}';
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SpotCategory that = (SpotCategory) o;

        return remote_id == that.remote_id;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }


    public String getIconLocalFilename() {
        return "ic_spot_category_"+ this.getRemoteId();
    }

    @Override
    public String getIconUrl() {
        return this.iconUrl;
    }
}
