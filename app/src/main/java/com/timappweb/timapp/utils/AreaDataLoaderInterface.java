package com.timappweb.timapp.utils;

import com.timappweb.timapp.rest.QueryCondition;

/**
 * Created by stephane on 12/9/2015.
 */
public interface AreaDataLoaderInterface {

    void load(IntPoint pCpy, AreaRequestItem request, QueryCondition conditions);

}

