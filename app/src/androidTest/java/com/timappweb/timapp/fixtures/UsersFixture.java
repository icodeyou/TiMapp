package com.timappweb.timapp.fixtures;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.auth.FacebookLoginProvider;
import com.timappweb.timapp.utils.facebook.FacebookApiHelper;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by Stephane on 24/09/2016.
 */
public class UsersFixture {

    public static void init(){

    }

    public static JsonObject createFacebookLoginPayload(String accessToken, String appId){
        return FacebookLoginProvider.createPayload(accessToken, appId);
    }


    public static JsonObject getLoginPayload(String facebookId){
        FacebookApiHelper.TestUser testUser = FacebookApiHelper.getUser(facebookId);

        assertNotNull("Cannot get any facebook access token to performs tests. Please make sure there are test user for the facebook app id "
                + MyApplication.getApplicationBaseContext().getString(R.string.facebook_app_id), testUser);


        String userAppId = FirebaseInstanceId.getInstance().getId();
        return createFacebookLoginPayload(testUser.getAccessToken(), userAppId);
    }
}
