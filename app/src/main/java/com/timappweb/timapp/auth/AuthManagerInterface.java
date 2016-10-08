package com.timappweb.timapp.auth;

import android.content.Context;

import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.managers.HttpCallManager;

/**
 * Created by Stephane on 24/09/2016.
 */
public interface AuthManagerInterface<ServerResponseType> {

    String getToken();

    void logout();

    HttpCallManager checkToken();

    boolean login(String providerId, ServerResponseType serverResponse) throws AuthManager.CannotLoginException;

    User getCurrentUser();

    boolean isLoggedIn();
}
