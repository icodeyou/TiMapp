package com.timappweb.timapp.config;

import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.entities.SocialProvider;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.KeyValueStorage;


/**
 * Created by stephane on 4/26/2016.
 */
public class AuthProvider {

    private static final String TAG                     = "AuthProvider";
    public static final int     TOKEN_CHECK_DELAY       = 3600;

    public static final String  KEY_TOKEN               = "token";
    public static final String  SOCIAL_PROVIDER_TOKEN   = "social_provider_token";
    public static final String  SOCIAL_PROVIDER_TYPE    = "social_provider_type";
    public static final String  KEY_IS_LOGIN            = "IsLoggedIn";
    private static final String KEY_LOGIN_TIME          = "LoginTime";
    public static final String  KEY_ID                  = "user.id";

    private boolean             _isUserLoaded           = false;
    private User                currentUser             = null;
    private SocialProvider      _socialProviderType;
    private String              _socialProviderToken;


    public String getToken() {
        return KeyValueStorage.out().getString(KEY_TOKEN, null);
    }
    public String getSocialProviderToken() {
        return KeyValueStorage.out().getString(SOCIAL_PROVIDER_TOKEN, null);
    }

    public void logout() {
        KeyValueStorage.clear(SOCIAL_PROVIDER_TOKEN, SOCIAL_PROVIDER_TYPE, KEY_TOKEN, KEY_IS_LOGIN, KEY_ID);
        currentUser = null;
    }

    public HttpCallManager checkToken() {
        long loginTime = KeyValueStorage.getSafeLong(KEY_LOGIN_TIME, 0);
        int tokenOld = (int) ((System.currentTimeMillis() - loginTime)/1000);

        if (tokenOld > TOKEN_CHECK_DELAY){
            Log.i(TAG, "Token is older that " + TOKEN_CHECK_DELAY + " sec (" + tokenOld + " seconds old)");
            return RestClient
                    .buildCall(RestClient.service().checkToken())
                    .onResponse(new HttpCallback() {
                        @Override
                        public void successful(Object feedback) {
                            Log.i(TAG, "Checking token OK");
                            KeyValueStorage.in().putLong(KEY_LOGIN_TIME, System.currentTimeMillis()).commit();
                        }

                        @Override
                        public void notSuccessful(){
                            Log.i(TAG, "Token is not valid anymore. Login out");
                            MyApplication.auth.logout();
                        }
                    });
        } else{
            Log.i(TAG, "Token is still valid (" + tokenOld + " seconds old)");
        }
        return null;
    }

    public boolean login(User user, String token, String accessToken) {
        try{
            setCurrentUser(user);
            KeyValueStorage.in()
                    .putString(KEY_TOKEN, token)
                    .putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
            setSocialProvider(SocialProvider.FACEBOOK, accessToken);
            return true;
        } catch (CannotSaveModelException e) {
            Log.e(TAG, "Cannot set current user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public User getCurrentUser() {
        if (!_isUserLoaded){
            _isUserLoaded = true;
            int userId = KeyValueStorage.out().getInt(KEY_ID, -1);
            if (userId == -1){
                return null;
            }
            currentUser = User.loadByRemoteId(User.class, userId);
            Log.d(TAG, "Loading user form pref: " + currentUser);
        }

        return currentUser;
    }

    public interface OnTokenListener{
        void onTokenValid();
        void onTokenOutdated();
        void onTokenFailure();
    }


    private void setCurrentUser(User user) throws CannotSaveModelException {
        currentUser = user.deepSave();
        _isUserLoaded = true;
        Log.i(TAG, "Writing user information: " + user);
        KeyValueStorage.in()
                .putInt(KEY_ID, user.remote_id)
                .putBoolean(KEY_IS_LOGIN, true)
                .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
                .commit();
    }


    private void setSocialProvider(SocialProvider provider, String accessToken) {
        this._socialProviderType = provider;
        this._socialProviderToken = accessToken;
        KeyValueStorage.in()
            .putString(SOCIAL_PROVIDER_TOKEN, _socialProviderToken)
            .putString(SOCIAL_PROVIDER_TYPE, _socialProviderType.toString())
            .commit();
    }


}
