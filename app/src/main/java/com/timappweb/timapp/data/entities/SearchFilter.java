package com.timappweb.timapp.data.entities;

import com.google.gson.annotations.Expose;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.Tag;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 3/19/2016.
 */
public class SearchFilter {

    @Expose
    public List<Tag> tags;

    @Expose
    public List<EventCategory> categories;

    public SearchFilter() {
        this.tags = new LinkedList<>();
        this.categories = new LinkedList<>();
    }
}
