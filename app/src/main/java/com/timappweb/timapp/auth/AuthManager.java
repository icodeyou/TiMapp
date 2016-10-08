package com.timappweb.timapp.auth;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.io.responses.RestFeedback;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.services.MyInstanceIDListenerService;
import com.timappweb.timapp.utils.KeyValueStorage;

import java.util.HashMap;


/**
 * Created by stephane on 4/26/2016.
 */
public class AuthManager implements AuthManagerInterface<JsonObject>{

    private static final String TAG                     = "AuthProvider";
    public static final int     TOKEN_CHECK_DELAY       = 3600;

    public static final String  KEY_TOKEN               = "token";
    public static final String  KEY_IS_LOGIN            = "IsLoggedIn";
    private static final String KEY_LOGIN_TIME          = "LoginTime";
    public static final String  KEY_ID                  = "user.id";

    // ---------------------------------------------------------------------------------------------

    private HashMap<String, AuthProviderInterface>  mAuthProviders = new HashMap<>();
    private boolean             _isUserLoaded           = false;
    private User                currentUser             = null;


    @Override
    public String getToken() {
        return KeyValueStorage.out().getString(KEY_TOKEN, null);
    }

    @Override
    public void logout() {
        KeyValueStorage.clear(KEY_TOKEN, KEY_IS_LOGIN, KEY_ID);
        FirebaseAuth.getInstance().signOut();
        currentUser = null;
    }

    @Override
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

    @Override
    public boolean login(String providerId, JsonObject feedback) throws CannotLoginException {
        try{
            int userId = feedback.get("id").getAsInt();
            User user = User.loadByRemoteId(User.class, userId);
            if (user == null) user = new User();

            String token = feedback.get("token").getAsString();
            user.username = feedback.get("username").getAsString();
            user.provider_uid = feedback.get("social_id").getAsString();
            user.provider = SocialProvider.FACEBOOK;
            user.remote_id = userId;
           // user.app_id = InstanceID.getInstance(context).getId();
            setCurrentUser(user);
            KeyValueStorage.in()
                    .putString(KEY_TOKEN, token)
                    .putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
            Log.i(TAG, "Trying to login user: " + user);
            requestGcmToken(MyApplication.getApplicationBaseContext());
            QuotaManager.sync();
            return true;
        } catch (CannotSaveModelException e) {
            throw new CannotLoginException("Cannot save model:" + e.getMessage());
        }
    }

    @Override
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

    @Override
    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public AuthManager addAuthProvider(AuthProviderInterface provider){
        this.mAuthProviders.put(provider.getId(), provider);
        return this;
    }

    public AuthProviderInterface getProvider(String providerId) {
        return this.mAuthProviders.get(providerId);
    }
    // ---------------------------------------------------------------------------------------------

    private static void requestGcmToken(Context context) {
        // TODO is it still usefull with firebase ?
        Log.d(TAG, "Starting IntentService to update user token");
        Intent intent = new Intent(context, MyInstanceIDListenerService.class);
        //intent.putExtra(Constants.RECEIVER, mResultReceiver);
        //intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        context.startService(intent);
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

    // ---------------------------------------------------------------------------------------------

    public class CannotLoginException extends Exception {
        public CannotLoginException(String s) {
            super(s);
        }
    }
}
