package com.timappweb.timapp.data.loader;

import com.timappweb.timapp.data.loader.paginate.CursorPaginateDataLoader;

/**
 * Created by Stephane on 30/10/2016.
 */
public interface BeforeLoadCallback {

    boolean beforeLoad(CursorPaginateDataLoader.LoadType loadType);

}
