package com.timappweb.timapp.data.loader.paginate;

import java.util.List;

/**
 * Created by Stephane on 24/10/2016.
 */

public class DBCacheEngine<T> implements CursorPaginateDataLoader.CacheEngine<T> {

    @Override
    public void init(String id) {

    }

    @Override
    public List<T> load() {
        return null;
    }

    protected void loadFromFile(){

    }

    protected void store(){

    }
}
