package com.timappweb.timapp.rest.model;

import java.util.HashMap;
import java.util.List;

/**
 * Created by stephane on 3/16/2016.
 */
public class PaginationResponse<T> {

    public int total;
    public int perPage;
    public List<T> items;
    public HashMap<String,String> extra;

}
