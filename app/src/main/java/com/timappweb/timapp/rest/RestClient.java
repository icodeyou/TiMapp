package com.timappweb.timapp.rest;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.config.LocalPersistenceManager;
import com.timappweb.timapp.entities.SocialProvider;
import com.timappweb.timapp.rest.model.RestFeedback;

import org.xml.sax.ErrorHandler;

import okhttp3.OkHttpClient;
//import okhttp3.logging.HttpLoggingInterceptor;
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

    //private static final String SQL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:SSSZ"; // http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
    private static RestClient conn = null;
    private final Application app;
    private final OkHttpClient httpClient;
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

    protected RestClient(Application app, String endpoint){
        this.app = app;

        Log.i(TAG, "Initializing server connection at " + endpoint);
        /*
        Gson gson = new GsonBuilder()
                .setDateFormat(SQL_DATE_FORMAT)
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .create();*/

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(new SessionRequestInterceptor());

        //HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        //logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder.addInterceptor(new LogRequestInterceptor());
        // Executor use to cancel pending request to the server
        // http://stackoverflow.com/questions/18131382/using-squares-retrofit-client-is-it-possible-to-cancel-an-in-progress-request
        //mExecutorService = Executors.newCachedThreadPool();
        this.httpClient = httpClientBuilder.build();
        builder = new Retrofit.Builder()
                //.setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.BASIC)
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .client(this.httpClient);

                //.setRequestInterceptor(new SessionRequestInterceptor())
                //.setConverter(new GsonConverter(gson))
                //.setExecutors(mExecutorService, new MainThreadExecutor());


        this.createService();
        Log.i(TAG, "Create connection with web service done!");

    }

    public void createService(){
        this.service =  builder.build().create(WebServiceInterface.class);
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

        // After logout redirect user to Login Activity
        Intent i = new Intent(app, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        app.startActivity(i);
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

    public void login(String token) {
        LocalPersistenceManager.in().putString(KEY_TOKEN, token);
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public String getProviderToken() {
        return _socialProviderToken;
    }

    public void setSocialProvider(SocialProvider provider, String accessToken) {
        this._socialProviderType = provider;
        this._socialProviderToken = accessToken;
    }

/*
    public static void stopPendingRequest() {
        List<Runnable> pendingAndOngoing = mExecutorService.shutdownNow();
        Log.d(TAG, "Stopping " + pendingAndOngoing.size() + " request(s) to the server");
    }
    */
}

