package com.timappweb.timapp.fixtures;

import com.facebook.AccessToken;
import com.facebook.login.LoginResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.auth.FacebookLoginProvider;
import com.timappweb.timapp.auth.SocialProvider;
import com.timappweb.timapp.utils.facebook.FacebookApiHelper;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by Stephane on 24/09/2016.
 */
public class UsersFixture {

    public static void init(){

    }

    public static Object getLoginPayload(SocialProvider provider, String facebookId){
        switch (provider){
            case FACEBOOK:
                FacebookApiHelper.TestUser testUser = FacebookApiHelper.getUser(facebookId);
                assertNotNull("Cannot get any facebook access token to performs tests. Please make sure there are test user for the facebook app id "
                        + MyApplication.getApplicationBaseContext().getString(R.string.facebook_app_id), testUser);
                return FacebookLoginProvider.createPayload(testUser.getAccessToken());
            default:
                throw new InternalError("Invalid provider:" + provider);
        }
    }

    public static int userIdWithTags() {
        return 42;
    }
}
