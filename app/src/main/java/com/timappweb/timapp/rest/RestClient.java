package com.timappweb.timapp.rest;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.config.AuthProvider;
import com.timappweb.timapp.data.entities.SocialProvider;
import com.timappweb.timapp.data.loader.SectionContainer;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Spot;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.io.interceptors.LogRequestInterceptor;
import com.timappweb.timapp.rest.io.interceptors.SessionRequestInterceptor;
import com.timappweb.timapp.rest.io.request.RestQueryParams;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.rest.io.deserializers.EventDeserializer;
import com.timappweb.timapp.rest.io.deserializers.JsonConfDeserializer;
import com.timappweb.timapp.rest.io.deserializers.SpotDeserializer;
import com.timappweb.timapp.rest.managers.MultipleHttpCallManager;
import com.timappweb.timapp.rest.io.responses.RestFeedback;
import com.timappweb.timapp.rest.services.RestInterface;
import com.timappweb.timapp.rest.services.WebServiceInterface;
import com.timappweb.timapp.configsync.SyncConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    private static final long HTTP_PARAM_READ_TIMEOUT = 40;
    private static final long HTTP_PARAM_CONNECTION_TIMEOUT = 60;

    //private static final String SQL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:SSSZ"; // http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
    private static RestClient conn = null;
    private static HttpCallback defaultHttpCallback;
    private final Application app;
    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final Gson gson;
    private final AuthProvider authProvider;
    private String _socialProviderToken = null;
    private SocialProvider _socialProviderType = null;


    private Retrofit retrofit;


    // KEY ID
    //public static final String KEY_SESSION_ID = "remote_id";

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public static RestClient instance(){
        return conn;
    }

    public static WebServiceInterface service(){
        return conn.getService();
    }


    public static void init(Application app, String ep, AuthProvider authProvider){
        conn = new RestClient(app, ep, authProvider);
        defaultHttpCallback = new HttpCallback() {
            @Override
            public void error() {
                Log.e(TAG, "Error ");
            }

            @Override
            public void notFound() {
                Log.e(TAG, "Not found ");
            }
        };
    }

    protected WebServiceInterface service;
    protected RestInterface restService;

    private static Retrofit.Builder builder = null;

    protected RestClient(Application app, String baseUrl, AuthProvider authProvider){
        this.app = app;
        this.baseUrl = baseUrl;
        this.authProvider = authProvider;

        Log.i(TAG, "Initializing server connection at " + baseUrl);
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new SessionRequestInterceptor())
                .addInterceptor(new LogRequestInterceptor())
                .readTimeout(HTTP_PARAM_READ_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(HTTP_PARAM_CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        this.httpClient = httpClientBuilder.build();


        this.gson =  new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(SyncConfig.class, new JsonConfDeserializer())
                .registerTypeAdapter(Event.class, new EventDeserializer())
                .registerTypeAdapter(Spot.class, new SpotDeserializer())
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
        this.retrofit = builder.build();
        this.service =  retrofit.create(WebServiceInterface.class);
        this.restService = retrofit.create(RestInterface.class);
    }

    public <T> T createService(Class<T> service){
        return builder.build().create(service);
    }


    public RestInterface getRestService(){
        return this.restService;
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
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Call<RestFeedback> logoutCall = this.service.logout();
        RestClient.buildCall(logoutCall)
                .onResponse(new HttpCallback() {
                    @Override
                    public void notSuccessful() {
                        Log.e(TAG, "Cannot logout user on server side...");
                    }

                    @Override
                    public void successful(Object feedback) {
                        Log.d(TAG, "User logged out on server side");
                    }
                })
                .perform();

        app.startActivity(i);
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }


    public static RestInterface restService() {
        return conn.getRestService();
    }

    public static HttpCallManager post(String url, JsonObject object) {
        Call call = RestClient.restService().post(url, object);
        return buildCall(call);
    }


    public static <T> HttpCallManager buildCall(Call<T> call) {
        return new HttpCallManager<>(call)
                .onResponse(defaultHttpCallback);
    }

    public static MultipleHttpCallManager mulipleCallsManager() {
        return new MultipleHttpCallManager();
    }

    public static RestQueryParams buildPaginatedOptions(SectionContainer.PaginatedSection section) {
        return new RestQueryParams()
                .add(RestQueryParams.SYNC_PARAM_MIN_ID, section.start)
                .add(RestQueryParams.SYNC_PARAM_MAX_ID, section.end)
                .add(RestQueryParams.SYNC_PARAM_DIRECTION, RestQueryParams.SyncDirection.DOWN.ordinal());
    }
}

