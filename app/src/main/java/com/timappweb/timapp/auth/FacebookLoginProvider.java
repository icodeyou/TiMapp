package com.timappweb.timapp.auth;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.timappweb.timapp.rest.RestClient;

import retrofit2.Call;

/**
 * Created by Stephane on 10/10/2016.
 */
public class FacebookLoginProvider implements AuthManager.LoginMethod<LoginResult> {

    public static JsonObject createPayload(String accessToken, String appId) {
        JsonObject object = new JsonObject();
        object.addProperty("access_token", accessToken);
        object.addProperty("app_id", appId);
        //object.addProperty("provider_id", SocialProvider.FACEBOOK.toString());
        return object;
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
}