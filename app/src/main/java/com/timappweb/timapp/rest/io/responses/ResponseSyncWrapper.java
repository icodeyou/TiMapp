package com.timappweb.timapp.rest.io.responses;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by Stephane on 16/08/2016.
 */
public class ResponseSyncWrapper<T> {

    @Expose
    public long last_update;

    @Expose
    public boolean up_to_date;

    @Expose
    public int limit;

    @Expose
    public List<T> items;

    @Expose
    public JsonElement extra;

    // ---------------------------------------------------------------------------------------------

    public int getCount(){
        return items != null ? items.size() : 0;
    }

    public T getLastItem() {
        return items.get(items.size() -1);
    }

    public T getFirstItem() {
        return items.get(0);
    }

    public int getLimit() {
        return limit;
    }
}
