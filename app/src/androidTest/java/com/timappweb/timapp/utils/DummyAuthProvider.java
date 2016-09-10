package com.timappweb.timapp.utils;

import com.timappweb.timapp.config.AuthProviderInterface;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.managers.HttpCallManager;

/**
 * Created by Stephane on 10/09/2016.
 */


public class DummyAuthProvider implements AuthProviderInterface {
    @Override
    public String getToken() {
        return "fejiopzjpf2938020ezpjfpezoCEDEfez";
    }

    @Override
    public String getSocialProviderToken() {
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
    public boolean login(User user, String token, String accessToken) {
        return false;
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
