package com.timappweb.timapp.auth;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.facebook.login.LoginResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.ServerErrorResponse;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.io.responses.RestFeedback;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.services.MyInstanceIDListenerService;
import com.timappweb.timapp.utils.JsonAccessor;
import com.timappweb.timapp.utils.KeyValueStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


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
    private List<AuthStateChangedListener> observers = new ArrayList<>();

    public AuthManager() {
        this.observers = new ArrayList<>();
    }

    @Override
    public String getToken() {
        return KeyValueStorage.out().getString(KEY_TOKEN, null);
    }

    @Override
    public void logout() {
        KeyValueStorage.clear(KEY_TOKEN, KEY_IS_LOGIN, KEY_ID);
        FirebaseAuth.getInstance().signOut();
        currentUser = null;

        this.notifyLogout();
    }

    public AuthManager registerListener(AuthStateChangedListener listener){
        this.observers.add(listener);
        return this;
    }

    public AuthManager removeListener(AuthStateChangedListener listener){
        this.observers.remove(listener);
        return this;
    }

    private void notifyLogout() {
        for (AuthStateChangedListener listener: this.observers){
            listener.onLogout();
        }
    }

    private void notifyLogin() {
        for (AuthStateChangedListener listener: this.observers){
            listener.onLogin();
        }
    }


    @Override
    public HttpCallManager checkToken() {
        long loginTime = KeyValueStorage.getSafeLong(KEY_LOGIN_TIME, 0);
        int tokenOld = (int) ((System.currentTimeMillis() - loginTime)/1000);

        if (tokenOld > ConfigurationProvider.rules().token_duration){
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

    public HttpCallManager remoteLogin(Call<JsonObject> call){
        return RestClient.buildCall(call)
                .onResponse(new HttpCallback<JsonObject>() {

                    @Override
                    public void successful(JsonObject feedback) throws CannotLoginException {
                        AuthManager.this.localLogin(new LoginFeedback(feedback));
                    }

                })
                .perform();
    }

    public boolean localLogin(LoginFeedback feedback) throws CannotLoginException {
        try {
            int userId = (int) feedback.getUserId();
            User user = User.loadByRemoteId(User.class, userId);
            if (user == null) user = new User();

            String token = feedback.getToken();
            user.username = feedback.getUsername();
            //user.provider_uid = feedback.get("social_id").getAsString();
            //user.provider = SocialProvider.FACEBOOK;
            user.remote_id = userId;
            user.avatar_url = feedback.getAvatarUrl();
            // user.app_id = InstanceID.getInstance(context).getId();
            setCurrentUser(user);
            KeyValueStorage.in()
                    .putString(KEY_TOKEN, token)
                    .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
                    .commit();
            Log.i(TAG, "Trying to localLogin user: " + user);
            requestGcmToken(MyApplication.getApplicationBaseContext());
            QuotaManager.sync();
            this.notifyLogin();
            return true;
        }
        catch (Exception ex){
            throw new CannotLoginException("Cannot login for now:" + ex.getMessage());
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

    public <InputType> HttpCallManager logWith(final LoginMethod<InputType> loginMethod, InputType data){
        Call call = loginMethod.login(data);
        return this.remoteLogin(call)
                .onFinally(new HttpCallManager.FinallyCallback() {
                    @Override
                    public void onFinally(Response response, Throwable error) {
                        if (error != null || !response.isSuccessful()){
                            loginMethod.cancelLogin();
                        }
                    }
                });
    }
    // ---------------------------------------------------------------------------------------------

    public class CannotLoginException extends Exception {
        public CannotLoginException(String s) {
            super(s);
        }
    }


    public interface AuthStateChangedListener{
        void onLogin();
        void onLogout();
    }

    public interface LoginMethod<InputType>{

        Call<JsonObject> login(InputType data);

        void cancelLogin();
    }

    public class LoginFeedback extends JsonAccessor{

        public LoginFeedback(JsonObject data) {
            super(data);
        }

        public long getUserId() throws MissingKeyException {
            return this.getNotNull("user.id").getAsLong();
        }

        public String getToken() throws MissingKeyException {
            return this.getNotNull("token").getAsString();
        }

        public String getUsername() throws MissingKeyException {
            return this.getNotNull("user.username").getAsString();
        }

        public String getAvatarUrl() throws MissingKeyException {
            return this.getNotNull("user.avatar_url").getAsString();
        }

    }
}
