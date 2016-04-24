package com.timappweb.timapp.entities;

import com.timappweb.timapp.data.models.EventCategory;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stephane on 3/19/2016.
 */
public class SearchFilter {

    public List<Tag> tags;
    public List<EventCategory> categories;

    public SearchFilter() {
        this.tags = new LinkedList<>();
        this.categories = new LinkedList<>();
    }
}
