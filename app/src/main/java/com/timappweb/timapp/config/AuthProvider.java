package com.timappweb.timapp.config;

import android.content.Context;
import android.util.Log;

import com.timappweb.timapp.data.entities.SocialProvider;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;
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
    private static final String KEY_CURRENT_USER        = "current_user";
    private static final String KEY_LOGIN_TIME          = "LoginTime";
    public static final String  KEY_NAME                = "user.name";
    public static final String  KEY_ID                  = "user.id";
    public static final String  KEY_EMAIL               = "user.email";
    
    private User                currentUser;
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
    }

    public void checkToken(Context context, final OnTokenListener tokenListener) {
        long loginTime = KeyValueStorage.getSafeLong(KEY_LOGIN_TIME, 0);
        int tokenOld = (int) ((System.currentTimeMillis() - loginTime)/1000);

        if (tokenOld > TOKEN_CHECK_DELAY){
            Log.i(TAG, "Token is older that " + TOKEN_CHECK_DELAY + " sec (" + tokenOld + " seconds old)");
            RestClient.instance().checkToken(new RestFeedbackCallback(context) {

                @Override
                public void onActionSuccess(RestFeedback feedback) {
                    Log.i(TAG, "Checking token OK");
                    KeyValueStorage.in().putLong(KEY_LOGIN_TIME, System.currentTimeMillis()).commit();
                    tokenListener.onTokenValid();
                }

                @Override
                public void onActionFail(RestFeedback feedback) {
                    Log.i(TAG, "Checking token FAIL");
                    tokenListener.onTokenOutdated();
                }

            });
        } else{
            Log.i(TAG, "Token is still valid (" + tokenOld + " seconds old)");
            tokenListener.onTokenValid();
        }
    }

    public void login(User user, String token, String accessToken) {
        KeyValueStorage.in()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        setCurrentUser(user);
        setSocialProvider(SocialProvider.FACEBOOK, accessToken);
    }

    public User getCurrentUser() {
        if (currentUser != null){
            return currentUser;
        }
        else {
            int userId = KeyValueStorage.out().getInt(KEY_ID, -1);
            if (userId == -1){
                return null;
            }
            currentUser = User.loadByRemoteId(User.class, userId);
            if (currentUser != null)
                return currentUser;

            currentUser = new User();
            currentUser.remote_id = userId;
            currentUser.email = KeyValueStorage.out().getString(KEY_EMAIL, null);
            currentUser.username = KeyValueStorage.out().getString(KEY_NAME, null);
            currentUser.save();
            Log.d(TAG, "Loading user form pref: " + currentUser);
            return currentUser;
        }
    }

    public interface OnTokenListener{
        void onTokenValid();
        void onTokenOutdated();
    }


    private void setCurrentUser(User user){
        Log.i(TAG, "Writing user information: " + user);
        KeyValueStorage.in()
            .putInt(KEY_ID, user.remote_id)
            .putString(KEY_NAME, user.username)
            .putString(KEY_EMAIL, user.email)
            .putBoolean(KEY_IS_LOGIN, true)
            .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            .commit();
        currentUser = user;
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
