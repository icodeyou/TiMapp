package com.timappweb.timapp.utils.idlingresource;

import android.support.test.espresso.IdlingResource;

import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.managers.HttpCallManager;

/**
 * Created by Stephane on 10/09/2016.
 */
public class ApiCallIdlingResource implements IdlingResource {

    private ResourceCallback callback;
    private boolean isIdle = false;

    public ApiCallIdlingResource() {

    }

    @Override
    public String getName() {
        return "ApiCallIdlingResource";
    }

    @Override
    public boolean isIdleNow() {
        isIdle = !RestClient.instance().hasPendingCall();
        if (isIdle && callback != null){
            callback.onTransitionToIdle();
        }
        return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.callback = resourceCallback;
    }


}
