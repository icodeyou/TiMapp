package com.timappweb.timapp.auth;

import android.content.Intent;
import android.hardware.camera2.params.Face;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.rest.RestClient;

import retrofit2.Call;

/**
 * Created by Stephane on 10/10/2016.
 */
public class FacebookLoginProvider implements AuthManager.LoginMethod<LoginResult, AccessToken> {
    private static final String TAG = "FacebookLoginProvider";

    AccessTokenTracker mAccessTokenTracker;

    public FacebookLoginProvider() {
        FacebookSdk.sdkInitialize(MyApplication.getApplicationBaseContext());
        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                mAccessTokenTracker.stopTracking();
                if(currentAccessToken == null) {
                    //(the user has revoked your permissions -
                    //by going to his settings and deleted your app)
                    //do the simple login to FaceBook
                    FacebookLoginProvider.this.onPermissionRevoked();
                }
                else {
                    //you've got the new access token now.
                    //AccessToken.getToken() could be same for both
                    //parameters but you should only use "currentAccessToken"
                    FacebookLoginProvider.this.onCurrentAccessTokenChanged(oldAccessToken, currentAccessToken);
                }
            }
        };
    }

    @Override
    public void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken){
        // TODO
        Log.i(TAG, "onCurrentAccessTokenChanged: " + currentAccessToken);
    }

    @Override
    public void onPermissionRevoked(){
        // TODO [critical] logout app
        Log.i(TAG, "onPermissionRevoked: ");
    }


    @Override
    public SocialProvider getId() {
        return SocialProvider.FACEBOOK;
    }

    @Override
    public Call<JsonObject> login(LoginResult loginResult) {
        JsonObject data = createPayload(loginResult.getAccessToken().getToken(), FirebaseInstanceId.getInstance().getId());
        return RestClient.service().facebookLogin(data);
    }

    @Override
    public void cancelLogin() {
        LoginManager.getInstance().logOut();
    }

    public String getAccessToken() throws AuthManager.NoProviderAccessTokenException{
        if (AccessToken.getCurrentAccessToken() != null){
            return AccessToken.getCurrentAccessToken().getToken();
        }
        throw new AuthManager.NoProviderAccessTokenException();
    }

    @Override
    public void restoreSession() {
        if (!FacebookSdk.isInitialized()){
            FacebookSdk.sdkInitialize(MyApplication.getApplicationBaseContext());
        }
    }


    public static JsonObject createPayload(String accessToken, String appId) {
        JsonObject object = new JsonObject();
        object.addProperty("access_token", accessToken);
        object.addProperty("app_id", appId);
        //object.addProperty("provider_id", SocialProvider.FACEBOOK.toString());
        return object;
    }

}