package com.timappweb.timapp.utils.AreaDataCaching;

import com.timappweb.timapp.entities.MarkerValueInterface;
import com.timappweb.timapp.entities.Post;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.utils.IntPoint;

import java.util.List;

/**
 * Created by stephane on 12/9/2015.
 */
public interface AreaDataLoaderInterface<T> {

    /**
     * Filll the AreaRequestItem with data according to conditions for the point pCpy
     * @param point
     * @param request
     * @param conditions
     */
    void load(IntPoint point, AreaRequestItem request, QueryCondition conditions);

    void clear(List<T> data);

    void clearAll();

}

