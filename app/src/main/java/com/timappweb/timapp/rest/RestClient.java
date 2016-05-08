package com.timappweb.timapp.rest;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.config.AuthProvider;
import com.timappweb.timapp.data.entities.SocialProvider;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.rest.services.WebServiceInterface;
import com.timappweb.timapp.configsync.SyncConfig;

import java.util.concurrent.TimeUnit;

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


    //private static final String SQL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:SSSZ"; // http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
    private static RestClient conn = null;
    private final Application app;
    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final Gson gson;
    private final AuthProvider authProvider;
    private String _socialProviderToken = null;
    private SocialProvider _socialProviderType = null;


    // KEY ID
    //public static final String KEY_SESSION_ID = "remote_id";

    public static RestClient instance(){
        return conn;
    }

    public static WebServiceInterface service(){
        return conn.getService();
    }


    public static void init(Application app, String ep, AuthProvider authProvider){
        conn = new RestClient(app, ep, authProvider);
    }

    protected WebServiceInterface service;

    private static Retrofit.Builder builder = null;

    protected RestClient(Application app, String baseUrl, AuthProvider authProvider){
        this.app = app;
        this.baseUrl = baseUrl;
        this.authProvider = authProvider;

        Log.i(TAG, "Initializing server connection at " + baseUrl);
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new SessionRequestInterceptor())
                .addInterceptor(new LogRequestInterceptor())
                .readTimeout(40, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS);
        this.httpClient = httpClientBuilder.build();


        this.gson =  new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(SyncConfig.class, new JsonConfDeserializer())
                .create();

        builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(this.httpClient);


        this.createService();
        Log.i(TAG, "Create connection with web service done!");

    }

    public Gson getGson(){
        return this.gson;
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
        this.authProvider.logout();

        // After logout redirect user to Login UserActivity
        Intent i = new Intent(app, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new UserActivity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Call<RestFeedback> call = this.service.logout();
            call.enqueue(new RestFeedbackCallback(app.getApplicationContext()) {
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


}

