package com.timappweb.timapp.utils;

import com.timappweb.timapp.auth.AuthManagerInterface;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.managers.HttpCallManager;

/**
 * Created by Stephane on 10/09/2016.
 */


public class DummyAuthManager implements AuthManagerInterface {
    @Override
    public String getToken() {
        return "fejiopzjpf2938020ezpjfpezoCEDEfez";
    }

    @Override
    public void logout() {

    }

    @Override
    public HttpCallManager checkToken() {
        return null;
    }


    @Override
    public User getCurrentUser() {
        return new User();
    }

    @Override
    public boolean isLoggedIn() {
        return true;
    }
}
