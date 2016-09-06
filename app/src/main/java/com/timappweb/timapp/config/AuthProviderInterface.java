package com.timappweb.timapp.config;

import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.entities.SocialProvider;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.KeyValueStorage;


/**
 * Created by stephane on 4/26/2016.
 */
public interface AuthProviderInterface {

    String getToken();

    String getSocialProviderToken();

    void logout();

    HttpCallManager checkToken();

    boolean login(User user, String token, String accessToken);

    User getCurrentUser();

    boolean isLoggedIn();

}
