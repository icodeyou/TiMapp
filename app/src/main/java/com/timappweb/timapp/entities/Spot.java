package com.timappweb.timapp.entities;

import java.util.ArrayList;
import java.util.List;

public class Spot {

    public int id;
    public String name;
    public List<Tag> tags;
    public String description;
    public int created;
    public double latitude;
    public double longitude;
    public int count_posts;
    public int category_id;

    public Spot(String name) {
        this.name = name;
    }

    public Spot(String name, List<Tag> tags) {
        this.name = name;
        this.tags = tags;
    }

    public static Spot createDummy() {
        List<Tag> dummyTags = new ArrayList<>();
        dummyTags.add(Tag.createDummy());
        dummyTags.add(Tag.createDummy());
        dummyTags.add(Tag.createDummy());
        return new Spot("DummySpot", dummyTags);
    }
}
