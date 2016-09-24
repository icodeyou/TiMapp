package com.timappweb.timapp.auth;

import android.content.Context;

import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.managers.HttpCallManager;

/**
 * Created by Stephane on 24/09/2016.
 */
public interface AuthManagerInterface<ServerResponseType> {

    String getToken();

    String getSocialProviderToken();

    void logout();

    HttpCallManager checkToken();

    boolean login(Context context, ServerResponseType serverResponse, String accessToken) throws AuthManager.CannotLoginException;

    User getCurrentUser();

    boolean isLoggedIn();
}
