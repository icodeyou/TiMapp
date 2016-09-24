package com.timappweb.timapp.utils.facebook;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.gson.JsonObject;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;

import org.json.JSONArray;

/**
 * Created by Stephane on 24/09/2016.
 *
 */
public class FacebookApiHelper {

    private static final String TAG = "FacebookApiHelper";
    private static String mAppId;
    private static String mSecretToken = "WCbQwangC-_k5S8-AVPGz4rFnTs";

    public static void init(){
        Context context = MyApplication.getApplicationBaseContext();
        init(context);
    }


    public static String buildAppAccessTokenString(){
        return mAppId + "|" + mSecretToken;
    }

    public static void init(Context context){
        mAppId = context.getString(R.string.facebook_app_id);

        Log.i(TAG, "@init() with app id: " + mAppId + " (access_token: " + buildAppAccessTokenString() + ")");
        FacebookSdk.sdkInitialize(context);

        //AccessToken accessToken = new AccessToken(buildAppAccessTokenString(), mAppId, mAppId, null, null, null, null, null);
        //AccessToken.setCurrentAccessToken(accessToken);

        //Log.i(TAG, "Using access token: " +  AccessToken.getCurrentAccessToken());
        //AccessToken.setCurrentAccessToken(AccessToken.crnew AccessToken("1731017757159847|WCbQwangC-_k5S8-AVPGz4rFnTs", mAppId, null);
    }

    /**
     * Get App test users
     * Base on graph: https://developers.facebook.com/docs/graph-api/reference/v2.7/app/accounts/test-users
     *
     * Response:
     *  - string id The user ID of the test user.
     *  - string access_token The access token for the test user and this app. This field is only visible if the test user has installed the app.
     *  - string login_url This URL will allow the test user account to be logged into. The URL will expire one hour after it is generated, or after the first time it is used.
     *
     * @return
     */
    public static GraphRequest getUsers(GraphRequest.Callback callback){
        return new GraphRequest(
                null, //AccessToken.getCurrentAccessToken(),
                "/"+mAppId+"/accounts/test-users?access_token=" + buildAppAccessTokenString(),
                null,
                HttpMethod.POST,
                callback
        );
    }


    public static GraphRequest getUserAccessToken(){
        return null;
    }
}
