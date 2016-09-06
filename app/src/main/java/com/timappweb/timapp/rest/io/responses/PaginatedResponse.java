package com.timappweb.timapp.rest.io.responses;

import com.google.gson.annotations.Expose;
import com.timappweb.timapp.data.models.SyncBaseModel;

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

    // ---------------------------------------------------------------------------------------------

    public int itemsCount() {
        return items == null ? 0 : items.size();
    }

    public T getLastItem() {
        return items.get(items.size() -1);
    }

    public T getFirstItem() {
        return items.get(0);
    }
}
