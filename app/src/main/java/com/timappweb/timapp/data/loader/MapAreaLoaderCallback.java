package com.timappweb.timapp.data.loader;

import com.timappweb.timapp.data.loader.paginate.PaginateDataLoader;
import com.timappweb.timapp.data.models.SyncBaseModel;


/**
 * Created by Stephane on 11/08/2016.
 */

public class MapAreaLoaderCallback<T extends SyncBaseModel> extends PaginateDataLoader<T> {

    private static final String         TAG                             = "MapAreaLoaderCallback";

    // ---------------------------------------------------------------------------------------------

    public MapAreaLoaderCallback() {
        super();
    }


}