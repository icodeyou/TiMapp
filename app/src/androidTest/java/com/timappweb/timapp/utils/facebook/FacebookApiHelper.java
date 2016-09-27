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
import com.timappweb.timapp.utils.TestUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;

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

    private static HashMap<String, TestUser> mUsers;

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
    public static HashMap<String, TestUser> getUsers(){
        if (mUsers == null){
            _loadTestsUser();
        }
        return mUsers;
    }

    public static TestUser getUser(String facebookId) {
        return getUsers().get(facebookId);
    }

    // ---------------------------------------------------------------------------------------------

    private static void _loadTestsUser(){
        mUsers = new HashMap<>();

        String query = "/"+mAppId+"/accounts/test-users";
        Bundle params = new Bundle();
        params.putString("access_token", buildAppAccessTokenString());
        //params.putString("access_token", buildAppAccessTokenString());
        Log.i(TAG, "Requesting app users to url: " + query);
        new GraphRequest(
                null, //AccessToken.getCurrentAccessToken(),
                query,
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response.getError() == null){
                            try {
                                JSONObject body = response.getJSONObject();
                                JSONArray items = body.getJSONArray("data");
                                for (int i = 0; i < items.length(); i++){
                                    try {
                                        TestUser user = new TestUser(items.getJSONObject(i));
                                        Log.v(TAG, "Adding test user: " + user);
                                        mUsers.put(user.getId(), user);
                                    } catch (JSONException e) {
                                        Log.e(TAG, "Cannot parse item " + i);
                                    }
                                }
                            } catch (JSONException e) {
                                throw new InternalError("Cannot parse response: " + response.toString());
                            }
                        }
                        else{
                            Log.e(TAG, "Cannot load test users from facebook response: " + response.toString());
                        }
                    }
                }
        ).executeAndWait();
    }

    // ---------------------------------------------------------------------------------------------

    public static class TestUser{

        private JSONObject mData;

        public TestUser(JSONObject mData) {
            this.mData = mData;
        }

        public String getAccessToken(){
            return _getString("access_token");
        }

        public String getId(){
            return _getString("id");
        }

        public String getName() {
            return _getString("name");
        }

        private String _getString(String key){
            try {
                return mData.getString(key);
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestUser testUser = (TestUser) o;

            return mData != null ? getId().equals(testUser.getId()) : testUser.getId() == null;

        }

        @Override
        public int hashCode() {
            return mData != null ? getId().hashCode() : 0;
        }

        @Override
        public String toString() {
            return "TestUser{" +
                    "id=" + getId() +
                    ", access_token=" + getAccessToken() +
                    ", name=" + getName() +
                    '}';
        }

    }
}
