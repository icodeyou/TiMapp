package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by stephane on 4/5/2016.
 */
@Table(name = "SpotCategory")
public class SpotCategory extends SyncBaseModel implements Serializable {

    // =============================================================================================

    @Expose
    @SerializedName("name")
    @Column(name = "Name")
    public String name;

    @Expose
    @SerializedName("position")
    @Column(name = "Position")
    public int position;

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

    public static SpotCategory createDummy() {
        return new SpotCategory("DummyCategory");
    }

    @Override
    public String toString() {
        return "SpotCategory{" +
                "id=" + remote_id +
                ", name='" + name + '\'' +
                ", position=" + position +
                '}';
    }
}
