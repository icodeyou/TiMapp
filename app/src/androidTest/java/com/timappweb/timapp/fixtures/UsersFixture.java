package com.timappweb.timapp.fixtures;

import android.hardware.camera2.params.Face;
import android.util.Log;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.auth.FacebookAuthProvider;
import com.timappweb.timapp.utils.facebook.FacebookApiHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * Created by Stephane on 24/09/2016.
 */
public class UsersFixture {

    // TODO create function to get a facebook user token

    public static HashMap<String, Object> loginPayloads = new HashMap<>();

    public static void init(){

    }

    public static JsonObject createFacebookLoginPayload(String accessToken, String appId){
        return FacebookAuthProvider.createPayload(accessToken, appId);
    }


    public static JsonObject getLoginPayload(String facebookId){
        FacebookApiHelper.TestUser testUser = FacebookApiHelper.getUser(facebookId);

        assertNotNull("Cannot get any facebook access token to performs tests. Please make sure there are test user for the facebook app id "
                + MyApplication.getApplicationBaseContext().getString(R.string.facebook_app_id), testUser);

        String userAppId = InstanceID.getInstance(MyApplication.getApplicationBaseContext()).getId();
        return createFacebookLoginPayload(testUser.getAccessToken(), userAppId);
    }
}
