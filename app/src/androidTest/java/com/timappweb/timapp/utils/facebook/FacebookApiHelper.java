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
import org.json.JSONException;

/**
 * Created by Stephane on 24/09/2016.
 *
 */
public class FacebookApiHelper {

    private static final String TAG = "FacebookApiHelper";
    private static String mAppId;
    private static String mSecretToken = "07a7d5929a61221b51a45b2f7864a56a"; // @warining
    private static String mAccessToken = null;
    private static String mRedirectUrl;

    public static void init(){
        Context context = MyApplication.getApplicationBaseContext();
        init(context);
    }


    public static String buildAppAccessTokenString(){
        if (mAccessToken != null){
            return mAccessToken;
        }

        new GraphRequest(
                null, //AccessToken.getCurrentAccessToken(),
                "oauth/access_token?client_id="+mAppId+"&client_secret=" + mSecretToken + "&grant_type=client_credentials&redirect_uri=" + mRedirectUrl,
                null,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        // TODO assert result
                        try {
                            mAccessToken = response.getJSONObject().getString("access_token");
                        } catch (JSONException e) {
                            mAccessToken = null;
                        }
                    }
                }
        ).executeAndWait();

        if (mAccessToken == null){
            throw new InternalError("Cannot get facebook access token for this app. Please check your credential");
        }

        return mAccessToken;
    }

    public static void init(Context context){
        FacebookSdk.sdkInitialize(context);
        mAppId = context.getString(R.string.facebook_app_id);
        mRedirectUrl = "http://timappweb.com";
        Log.i(TAG, "@init() with app id: " + mAppId + " (access_token: " + buildAppAccessTokenString() + ")");
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
        String query = "/"+mAppId+"/accounts/test-users";
        Bundle params = new Bundle();
        params.putString("access_token", buildAppAccessTokenString());
        //params.putString("access_token", buildAppAccessTokenString());
        Log.i(TAG, "Requesting app users to url: " + query);
        return new GraphRequest(
                null, //AccessToken.getCurrentAccessToken(),
                query,
                params,
                HttpMethod.POST,
                callback
        );
    }


    public static GraphRequest getUserAccessToken(){
        return null;
    }
}
