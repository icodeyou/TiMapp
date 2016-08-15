package com.timappweb.timapp.utils.AreaDataCaching;

import com.timappweb.timapp.rest.io.request.QueryCondition;
import com.timappweb.timapp.utils.IntPoint;

/**
 * Created by stephane on 12/9/2015.
 */
public interface AreaDataLoaderInterface<T> {

    /**
     * Filll the RAMAreaRequestItem with data according to conditions for the point pCpy
     * @param point
     * @param request
     * @param conditions
     */
    void load(IntPoint point, AreaRequestItemInterface request, QueryCondition conditions);

}

