package com.timappweb.timapp.auth;

import android.util.Log;

import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.io.responses.RestFeedback;
import com.timappweb.timapp.rest.managers.HttpCallManager;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Stephane on 24/09/2016.
 */
public class FacebookAuthProvider implements AuthProviderInterface<JsonObject,RestFeedback> {

    public static final String PROVIDER_ID = "facebook";
    private static final String TAG = "FacebookAuthProvider";

    public void logout(){
        LoginManager.getInstance().logOut();
    }

    public HttpCallManager login(JsonObject payload, final AuthAttemptCallback<RestFeedback> callback){
        Log.i(TAG, "Request login with " + this.getId() + ". Payload=" + payload);
        Call<RestFeedback> call = RestClient.service().facebookLogin(payload);
        return RestClient.buildCall(call)
                .onResponse(new HttpCallback<RestFeedback>() {
                    @Override
                    public void successful(RestFeedback feedback) {
                        try{
                            MyApplication
                                    .getAuthManager()
                                    .login(PROVIDER_ID, feedback.data);
                            callback.onSuccess(feedback);
                        }
                        catch (Exception ex){
                            Log.e(TAG, "Cannot parse server response for login: " + ex.getMessage());
                            ex.printStackTrace();
                            callback.onFailure(ex);
                        }
                    }

                })
                .onFinally(new HttpCallManager.FinallyCallback() {
                    @Override
                    public void onFinally(Response response, Throwable error) {
                        if (error != null){
                            callback.onFailure(error);

                        }
                        else if (!response.isSuccessful()){
                            callback.onFailure(new ServerErrorResponse(response));
                        }
                    }
                })
                .perform();
    }

    @Override
    public String getId() {
        return FacebookAuthProvider.PROVIDER_ID;
    }

    public static JsonObject createPayload(String accessToken, String appId) {
        JsonObject object = new JsonObject();
        object.addProperty("access_token", accessToken);
        object.addProperty("app_id", appId);
        return object;
    }
    /*
    public static JsonObject createPayload(String accessToken, String appId) {
        JsonObject object = new JsonObject();
        object.addProperty("access_token", accessToken);
        object.addProperty("app_id", appId);
        return object;
    }*/
}
