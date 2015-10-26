package com.timappweb.timapp.rest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.LocalPersistenceManager;

import retrofit.RequestInterceptor;

/**
 * Created by stephane on 9/12/2015.
 */
public class SessionRequestInterceptor implements RequestInterceptor
{
    @Override
    public void intercept(RequestInterceptor.RequestFacade request)
    {
        if (MyApplication.isLoggedIn()) {
            request.addHeader("Authorization", "Bearer " + LocalPersistenceManager.instance.pref.getString(RestClient.KEY_TOKEN, null));
            request.addHeader("Accept", "application/json");
        }
    }
}

