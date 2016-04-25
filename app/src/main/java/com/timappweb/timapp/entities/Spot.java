package com.timappweb.timapp.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.timappweb.timapp.data.models.SpotCategory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Spot implements Serializable {

    @Expose
    @SerializedName("id")
    public int id;

    @Expose
    @SerializedName("name")
    public String name;

    @Expose
    @SerializedName("description")
    public String description;

    @Expose
    @SerializedName("latitude")
    public double latitude;

    @Expose
    @SerializedName("longitude")
    public double longitude;

    @Expose
    @SerializedName("spot_category_id")
    public int category_id;

    @Expose(deserialize = true, serialize = false)
    public int created;

    @Expose(deserialize = true, serialize = false)
    public List<Tag> tags;

    @SerializedName("spot_category")
    @Expose(deserialize = true, serialize = false)
    public SpotCategory category;

    // =============================================================================================

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
            this.category_id = category.id;
        }
    }

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
                ", id=" + id +
                ", category_id=" + category_id +
                '}';
    }
}
