package com.timappweb.timapp.auth;

import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.managers.HttpCallManager;

/**
 * Created by Stephane on 24/09/2016.
 */
public interface AuthManagerInterface {

    String getToken();

    void logout();

    HttpCallManager checkToken();

    User getCurrentUser();

    boolean isLoggedIn();
}
