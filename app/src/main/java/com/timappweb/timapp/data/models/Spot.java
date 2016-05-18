package com.timappweb.timapp.data.models;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Table(name = "Spot")
public class Spot extends SyncBaseModel implements Serializable {

    // =============================================================================================
    // DATABASE

    @Column(name = "Name")
    @Expose
    @SerializedName("name")
    public String name;

    @Column(name = "Description")
    @Expose
    @SerializedName("description")
    public String description;

    @Column(name = "Latitude")
    @Expose
    @SerializedName("latitude")
    public double latitude;

    @Column(name = "Longitude")
    @Expose
    @SerializedName("longitude")
    public double longitude;

    @Column(name = "CategoryId")
    @Expose
    @SerializedName("spot_category_id")
    public int category_id;

    @Column(name = "Created")
    @Expose(deserialize = true, serialize = false)
    public int created;

    // =============================================================================================
    // Fields

    @SerializedName("tags")
    @Expose(deserialize = true, serialize = false)
    public List<Tag> tags;

    @SerializedName("spot_category")
    @Expose(deserialize = true, serialize = false)
    public SpotCategory category;

    // =============================================================================================

    public Spot() {}

    public Spot(String name) {
        this.name = name;
    }

    public Spot(String name, List<Tag> tags) {
        this.name = name;
        this.tags = tags;
    }

    public Spot(String name, SpotCategory category) {
        this.name = name;
        this.category = category;
        if (category != null){
            this.category_id = category.remote_id;
        }
    }

    // =============================================================================================

    public static Spot createDummy() {
        List<Tag> dummyTags = new ArrayList<>();
        dummyTags.add(Tag.createDummy());
        dummyTags.add(Tag.createDummy());
        dummyTags.add(Tag.createDummy());
        return new Spot("DummySpot", dummyTags);
    }

    @Override
    public String toString() {
        return "Spot{" +
                "name='" + name + '\'' +
                ", id=" + remote_id +
                ", category_id=" + category_id +
                '}';
    }

    @Override
    public boolean isSync(SyncBaseModel model) {
        return false;
    }
}
