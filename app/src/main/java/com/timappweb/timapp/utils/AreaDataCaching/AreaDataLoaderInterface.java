package com.timappweb.timapp.utils.AreaDataCaching;

import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.utils.IntPoint;

/**
 * Created by stephane on 12/9/2015.
 */
public interface AreaDataLoaderInterface {

    void load(IntPoint pCpy, AreaRequestItem request, QueryCondition conditions);

}

