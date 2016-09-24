package com.timappweb.timapp.auth;

import com.timappweb.timapp.rest.managers.HttpCallManager;


/**
 * Created by stephane on 4/26/2016.
 */
public interface AuthProviderInterface<LoginPayloadType, ResponseType> {

    HttpCallManager login(LoginPayloadType payload, AuthAttemptCallback<ResponseType> callback);

    String getId();

    interface AuthAttemptCallback<T>{

        void onSuccess(T feedback);

        void onFailure(Throwable exception);

    }
}
