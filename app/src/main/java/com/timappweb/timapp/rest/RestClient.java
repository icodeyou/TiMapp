package com.timappweb.timapp.rest;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.auth.AuthManagerInterface;
import com.timappweb.timapp.data.loader.sections.SectionContainer;
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

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
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
    private static final long HTTP_PARAM_READ_TIMEOUT = 30;
    private static final long HTTP_PARAM_CONNECTION_TIMEOUT = 35;

    private static RestClient conn = null;
    //private static HttpCallback defaultHttpCallback;
    private final Application app;                      // TODO remove this dependency. It will make it easier to Unit test
    private final GsonBuilder gsonBuilder;
    private final OkHttpClient.Builder httpClientBuilder;
    private OkHttpClient httpClient;
    private final String baseUrl;
    private Gson gson;
    private final AuthManagerInterface authManager;
    private LinkedList<HttpCallManager> pendingCalls = new LinkedList<>();
    private Retrofit retrofit;
    protected WebServiceInterface service;
    protected RestInterface restService;


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


    public static void init(Application app, String baseUrl, AuthManagerInterface authProvider){
        conn = new RestClient(app, baseUrl, authProvider);
    }

    private static Retrofit.Builder retrofitBuilder = null;

    /**
     * TODO replace by a factory
     * @param app
     * @param baseUrl
     * @param authProvider
     */
    protected RestClient(Application app, String baseUrl, AuthManagerInterface authProvider){
        this.app = app;
        this.baseUrl = baseUrl;
        this.authManager = authProvider;
        this.pendingCalls = new LinkedList<>();
        this.httpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new SessionRequestInterceptor(authManager))
                .addInterceptor(new LogRequestInterceptor())
                .readTimeout(HTTP_PARAM_READ_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(HTTP_PARAM_CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        this.gsonBuilder = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(SyncConfig.class, new JsonConfDeserializer())
                .registerTypeAdapter(Event.class, new EventDeserializer())
                .registerTypeAdapter(Spot.class, new SpotDeserializer());
        Log.i(TAG, "Initializing server connection at " + baseUrl);
        this.buildAll();
        Log.i(TAG, "Create connection with web service done!");

    }

    public void buildAll() {
        this.gson =  gsonBuilder.create();
        this.httpClient = getHttpBuilder().build();
        this.retrofitBuilder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(this.httpClient);
        this.retrofit = retrofitBuilder.build();
        this.service =  retrofit.create(WebServiceInterface.class);
        this.restService = retrofit.create(RestInterface.class);
        Log.i(TAG, "Building rest client done!");
    }

    public OkHttpClient.Builder getHttpBuilder(){
        return httpClientBuilder;
    }

    public Gson getGson(){
        return this.gson;
    }

    public <T> T createService(Class<T> service){
        return retrofit.create(service);
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
        this.authManager.logout();

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
    public static HttpCallManager post(String url,  JsonObject object, RequestBody file) {
        Call call = RestClient.restService().post(url, object, file);
        return buildCall(call);
    }

    public static <T> HttpCallManager buildCall(final Call<T> call) {

        final HttpCallManager<T> callManager = new HttpCallManager<>(call);
        RestClient.instance().pendingCalls.add(callManager);
        callManager
                .onFinally(new HttpCallManager.FinallyCallback<T>(){
                    @Override
                    public void onFinally(Response response, Throwable error) {
                        RestClient.instance().pendingCalls.remove(callManager);
                    }
                });

        return callManager;
    }

    public static MultipleHttpCallManager mulipleCallsManager() {
        return new MultipleHttpCallManager();
    }

    public static RestQueryParams buildPaginatedOptions(SectionContainer.PaginatedSection section) {
        RestQueryParams query = new RestQueryParams();
        if (section.getStart() != -1){
            query.add(RestQueryParams.SYNC_PARAM_MAX_ID, section.start);
        }
        if (section.getEnd() != -1){
            query.add(RestQueryParams.SYNC_PARAM_MIN_ID, section.end);
        }
        return query.add(RestQueryParams.SYNC_PARAM_DIRECTION, RestQueryParams.SyncDirection.DOWN.ordinal());
    }

    // ---------------------------------------------------------------------------------------------
    // For testing purpose

    public static boolean hasPendingCall() {
        for (HttpCallManager call: RestClient.instance().pendingCalls){
            if (!call.isDone()){
                return true;
            }
        }
        return false;
    }

}

