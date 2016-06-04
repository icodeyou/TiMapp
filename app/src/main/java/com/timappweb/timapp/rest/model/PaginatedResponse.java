package com.timappweb.timapp.rest.model;

import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.List;

/**
 * Created by stephane on 3/16/2016.
 */
public class PaginatedResponse<T> {

    @Expose
    public int total;

    @Expose
    public int perPage;

    @Expose
    public List<T> items;

    @Expose
    public HashMap<String,String> extra;

}
