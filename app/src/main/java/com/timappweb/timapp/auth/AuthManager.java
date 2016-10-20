package com.timappweb.timapp.auth;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.exceptions.CannotSaveModelException;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
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
public class AuthManager implements AuthManagerInterface{

    private static final String TAG                     = "AuthProvider";
    public static final int     TOKEN_CHECK_DELAY       = 3600;

    public static final String  KEY_TOKEN               = "token";
    public static final String  KEY_IS_LOGIN            = "IsLoggedIn";
    private static final String KEY_LOGIN_TIME          = "LoginTime";
    public static final String  KEY_ID                  = "user.id";
    public static final String KEY_PROVIDER_ID = "provider.id";

    // ---------------------------------------------------------------------------------------------

    //private HashMap<String, LoginMethod>  mAuthProviders = new HashMap<>();
    private boolean             _isUserLoaded           = false;
    private User                currentUser             = null;
    private List<AuthStateChangedListener> observers = new ArrayList<>();

    private HashMap<SocialProvider, LoginMethod> mAuthProviders;
    private SocialProvider mCurrentProvider;

    public AuthManager() {
        this.observers = new ArrayList<>();
        this.mAuthProviders = new HashMap<>();
    }

    @Override
    public String getToken() {
        return KeyValueStorage.out().getString(KEY_TOKEN, "");
    }

    public String getProviderToken() throws NoProviderAccessTokenException {
        if (getCurrentProvider() == null){
            Log.e(TAG, "User is not logged in with a provider");
            throw new NoProviderAccessTokenException();
        }
        return getCurrentProvider().getAccessToken();
    }

    public LoginMethod getCurrentProvider(){
        if (mCurrentProvider != null){
            return this.mAuthProviders.get(this.mCurrentProvider);
        }
        else{
            return null;
        }
    }

    @Override
    public void logout() {
        KeyValueStorage.clear(KEY_TOKEN, KEY_IS_LOGIN, KEY_ID, KEY_PROVIDER_ID);
        FirebaseAuth.getInstance().signOut();
        currentUser = null;
        mCurrentProvider = null;

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

    public boolean localLogin(LoginMethod loginMethod, LoginFeedback feedback) throws CannotLoginException {
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
                    .putString(KEY_PROVIDER_ID, loginMethod.getId().toString())
                    .commit();
            Log.i(TAG, "Trying to localLogin user: " + user);

            MyApplication.updateGoogleMessagingToken(MyApplication.getApplicationBaseContext(),
                    FirebaseInstanceId.getInstance().getToken());
            requestGcmToken(MyApplication.getApplicationBaseContext());
            QuotaManager.sync();
            this.notifyLogin();
            return true;
        }
        catch (Exception ex){
            throw new CannotLoginException("Cannot login for now:" + ex.getMessage());
        }
    }

    public void restoreSession(){
        String providerStr = KeyValueStorage.out().getString(KEY_PROVIDER_ID, "");
        SocialProvider provider = SocialProvider.fromString(providerStr);
        if (provider != null && this.hasProvider(provider)){
            Log.i(TAG, "Restoring session with provider: " + provider);
            this.getProvider(provider).restoreSession();
            mCurrentProvider = provider;
        }
    }

    private boolean hasProvider(SocialProvider provider) {
        return mAuthProviders.containsKey(provider);
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
            this.restoreSession();
        }

        return currentUser;
    }

    @Override
    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public <T extends LoginMethod> T getProvider(SocialProvider providerId) {
        return (T) this.mAuthProviders.get(providerId);
    }

    // ---------------------------------------------------------------------------------------------

    private static void requestGcmToken(Context context) {
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

    public <InputType> HttpCallManager logWith(final LoginMethod<InputType, ? extends Object> loginMethod, InputType data){
        Call call = loginMethod.login(data);

        return RestClient.buildCall(call)
                .onResponse(new HttpCallback<JsonObject>() {

                    @Override
                    public void successful(JsonObject feedback) throws CannotLoginException {
                        AuthManager.this.localLogin(loginMethod, new LoginFeedback(feedback));
                        mCurrentProvider = loginMethod.getId();
                    }

                })
                .onFinally(new HttpCallManager.FinallyCallback() {
                    @Override
                    public void onFinally(Response response, Throwable error) {
                        if (error != null || !response.isSuccessful()){
                            loginMethod.cancelLogin();
                        }
                    }
                })
                .perform();
    }

    public AuthManager addProvider(LoginMethod instance) {
        this.mAuthProviders.put(instance.getId(), instance);
        return this;
    }

    public boolean isLoggedWithProvider(SocialProvider provider, String uid) {
        try {
            return mCurrentProvider != null
                    && mCurrentProvider == provider
                    && getProvider(provider).getUid().equals(uid);
        } catch (NoProviderAccessTokenException e) {
            return false;
        }
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

    public interface LoginMethod<InputType, AccessTokenType>{

        SocialProvider getId();

        Call<JsonObject> login(InputType data);

        void cancelLogin();

        void onCurrentAccessTokenChanged(AccessTokenType oldAccessToken, AccessTokenType currentAccessToken);

        void onPermissionRevoked();

        String getAccessToken() throws AuthManager.NoProviderAccessTokenException;

        void restoreSession();

        String getUid() throws NoProviderAccessTokenException;
    }

    public static class NoProviderAccessTokenException extends Exception{
        public NoProviderAccessTokenException() {
            super("No provider token");
        }
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
