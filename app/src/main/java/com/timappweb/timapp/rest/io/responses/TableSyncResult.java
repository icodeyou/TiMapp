package com.timappweb.timapp.rest.io.responses;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by Stephane on 16/08/2016.
 */
public class TableSyncResult<T> {

    @Expose
    public long last_update;

    @Expose
    public boolean up_to_date;

    @Expose
    public List<T> items;

    @Expose
    public JsonElement extra;
}
