package com.timappweb.timapp.fixtures;

import android.util.Log;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.auth.FacebookAuthProvider;
import com.timappweb.timapp.utils.facebook.FacebookApiHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Stephane on 24/09/2016.
 */
public class UsersFixture {

    // TODO create function to get a facebook user token

    public static HashMap<String, Object> loginPayloads = new HashMap<>();

    public static void init(){
        loginPayloads.put("facebook_user", createFacebookLoginPayload(
                "EAAYmWfqD8acBAHfBtcmSZBYvcWD0qrQuYcSgN4Am4VYZC2LsWpnUQJH9EvvuRijIqhAE6guubiwrAMlPWSZBaZBv0Mz5ipaIBXeZAmn0S0RdaHwfXMcyrxZBrNPPCXXWOPKzwZALIeSfK3MR0dZBc2sF9burlmqknSzWXhMBtEZA4BN36ZAmKgBBW0",
                ""));
    }

    public static JsonObject createFacebookLoginPayload(String accessToken, String appId){
        return FacebookAuthProvider.createPayload(accessToken, appId);
    }

    public static <T> T getLoginPayload(String id){
        final JSONObject[] data = new JSONObject[1];
        FacebookApiHelper.getUsers(
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        data[0] = response.getJSONObject();
                    }
                }).executeAndWait();

        try {
            String userAppId = InstanceID.getInstance(MyApplication.getApplicationBaseContext()).getId();
            return (T) createFacebookLoginPayload(data[0].getString("access_token"), userAppId);
        } catch (JSONException e) {
            throw new InternalError("Test bad config: " + e.getMessage());
        }
    }
}
