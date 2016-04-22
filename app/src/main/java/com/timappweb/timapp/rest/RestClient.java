package com.timappweb.timapp.rest;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.config.LocalPersistenceManager;
import com.timappweb.timapp.entities.SocialProvider;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.rest.services.WebServiceInterface;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;

/**
 * Created by stephane on 8/21/2015.
 * Handle a connection with the webservice.
 * Simulate a session with the server when the user is logged in.
 *
 * http://blog.robinchutaux.com/blog/a-smart-way-to-use-retrofit/
 */
public class RestClient {

    private static final String TAG = "RestClient";
    private static final String SQL_DATE_FORMAT = "yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'";
    public static final String KEY_TOKEN = "token";
    private static final String SOCIAL_PROVIDER_TOKEN = "social_provider_token";
    private static final String SOCIAL_PROVIDER_TYPE = "social_provider_type";

    //private static final String SQL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:SSSZ"; // http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
    private static RestClient conn = null;
    private final Application app;
    private final OkHttpClient httpClient;
    private final String baseUrl;
    private String _socialProviderToken = null;
    private SocialProvider _socialProviderType = null;

    // KEY ID
    //public static final String KEY_SESSION_ID = "id";

    public static RestClient instance(){
        return conn;
    }

    public static WebServiceInterface service(){
        return conn.getService();
    }


    public static void init(Application app, String ep){
        conn = new RestClient(app, ep);
    }

    protected WebServiceInterface service;

    private static Retrofit.Builder builder = null;

    protected RestClient(Application app, String baseUrl){
        this.app = app;
        this.baseUrl = baseUrl;

        Log.i(TAG, "Initializing server connection at " + baseUrl);
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(new SessionRequestInterceptor());
        httpClientBuilder.addInterceptor(new LogRequestInterceptor());
        this.httpClient = httpClientBuilder.build();

        builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(this.httpClient);

        this.createService();
        Log.i(TAG, "Create connection with web service done!");

    }


    public void createService(){
        this.service =  builder.build().create(WebServiceInterface.class);
    }

    public <T> T createService(Class<T> service){
        return builder.build().create(service);
    }



    public WebServiceInterface getService(){
        return this.service;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        LocalPersistenceManager.in().clear();
        LocalPersistenceManager.in().commit();

        // After logout redirect user to Login UserActivity
        Intent i = new Intent(app, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new UserActivity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Call<RestFeedback> call = this.service.logout();
            call.enqueue(new RestFeedbackCallback() {
                @Override
                public void onActionSuccess(RestFeedback feedback) {
                    Log.d(TAG, "User logged out on server side");
                }

                @Override
                public void onActionFail(RestFeedback feedback) {
                    Log.e(TAG, "Cannot logout user on server side...");
                }
            });

        // Staring Login UserActivity
        app.startActivity(i);
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Check login on the server side thanks to the token
     */
    public void checkToken(RestFeedbackCallback callback) {
        Log.i(TAG, "Checking user token...");
        Call<RestFeedback> call = this.service.checkToken();
        call.enqueue(callback);
    }

    public String getToken() {
        return LocalPersistenceManager.out().getString(KEY_TOKEN, null);
    }
    public String getSocialProviderToken() {
        return LocalPersistenceManager.out().getString(SOCIAL_PROVIDER_TOKEN, null);
    }

    public void login(String token) {
        LocalPersistenceManager.in().putString(KEY_TOKEN, token);
    }

    public void setSocialProvider(SocialProvider provider, String accessToken) {
        this._socialProviderType = provider;
        this._socialProviderToken = accessToken;
        LocalPersistenceManager.in().putString(SOCIAL_PROVIDER_TOKEN, _socialProviderToken);
        LocalPersistenceManager.in().putString(SOCIAL_PROVIDER_TYPE, _socialProviderType.toString());
    }


}

