package com.timappweb.timapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.config.AuthProvider;
import com.timappweb.timapp.config.AuthProvider.OnTokenListener;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.entities.SearchFilter;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.model.RestFeedback;
import com.timappweb.timapp.services.RegistrationIntentService;
import com.timappweb.timapp.sync.AbstractSyncAdapter;
import com.timappweb.timapp.sync.ConfigSyncAdapter;
import com.timappweb.timapp.sync.UserSyncAdapter;
import com.timappweb.timapp.utils.ImagePipelineConfigFactory;
import com.timappweb.timapp.utils.KeyValueStorage;

import net.danlew.android.joda.JodaTimeAndroid;

import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import retrofit2.Call;

public class MyApplication extends com.activeandroid.app.Application {

    private static final String TAG = "MyApplication";
    private static final int TOKEN_CHECK_DELAY = 3600; // Check token every 1 hour

    public static SearchFilter searchFilter = new SearchFilter();
    private static DeferredObject deferred;
    private int notifyCount = 0;

    public static AuthProvider auth;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //MultiDex.install(this);
    }


    /**
     * @return true if user is logged in
     */
    public static boolean isLoggedIn(){
        return getCurrentUser() != null;
    }

    public static boolean isCurrentUser(int userId){
        return MyApplication.isLoggedIn() && MyApplication.getCurrentUser().remote_id == userId;
    }
    public static boolean isCurrentUser(User mUser) {
        return MyApplication.isCurrentUser(mUser.remote_id);
    }

    public void checkToken(){
        auth.checkToken(new OnTokenListener() {
            @Override
            public void onTokenValid() {
                Log.i(TAG, "Token is still valid.");
                notifyLoadingState("Token is still valid");
            }

            @Override
            public void onTokenOutdated() {
                Log.i(TAG, "Token is not valid anymore. Login out");
                notifyLoadingState("User token is not valid");
            }

            @Override
            public void onTokenFailure() {
                notifyLoadingState("Cannot get user token");
            }
        });

    }

    private void notifyLoadingState(String s) {
        notifyCount++;
        Log.i(TAG, "Notify loading state " + notifyCount + "/2 : " + s);
        if (deferred.isPending()){
            deferred.notify(s);
            if (notifyCount == 2){
                deferred.resolve(null);
            }
        }
    }

    public static User getCurrentUser(){
        return auth.getCurrentUser();
    }

    public static void login(Context context, User user, String token, String accessToken){
        auth.login(user, token, accessToken);
        UserSyncAdapter.syncImmediately(context);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        this.deferred = new DeferredObject();

        //this.deleteDatabase(getString(R.string.db_name));

        Fresco.initialize(this, ImagePipelineConfigFactory.getImagePipelineConfig(this));
        MyApplication.auth = new AuthProvider();
        RestClient.init(this, getResources().getString(R.string.ws_endpoint), MyApplication.auth);
        KeyValueStorage.init(this, RestClient.instance().getGson());
        initFacebookPermissions(); // Useless because friend are loaded from the server ... ? TODO move from here
        JodaTimeAndroid.init(this);
        QuotaManager.init(getApplicationContext()); // TODO must be unitialized only for logged in users
        AbstractSyncAdapter.initializeSyncAdapter(this);

        ConfigurationProvider.init(new ConfigurationProvider.OnConfigurationLoadedListener() {
            @Override
            public void onLoaded(String key) {
                notifyLoadingState("Server configuration loaded");
            }

            @Override
            public void onFail(String key) {
                Log.e(TAG, "Cannot load server configuration");
                IntentsUtils.error(MyApplication.this, R.string.fatal_error_server_down_title, R.string.fatal_error_unknown_reason_message);
            }
        });

        // If first start we need to wait for configuration from the server
        if (ConfigurationProvider.hasFullConfiguration()){
            notifyLoadingState("Server configuration loaded");
        }
        else{
            // If new installed app, this will be triggered two times... Not good
            ConfigSyncAdapter.syncImmediately(this);
        }

        checkToken();
    }


    private void initFacebookPermissions() {
        Permission[] permissions = new Permission[] {
                Permission.USER_PHOTOS,
                Permission.EMAIL,
                Permission.USER_FRIENDS,
        };
        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId(getResources().getString(R.string.app_id))
                .setNamespace(getResources().getString(R.string.namespace))
                .setPermissions(permissions)
                .build();
        SimpleFacebook.setConfiguration(configuration);
    }


    public static ImageView setCategoryBackground(ImageView i, int level) {
        switch (level) {
            case 0:
                i.setBackgroundResource(R.drawable.b1);
                return i;
            case 1:
                i.setBackgroundResource(R.drawable.b2);
                return i;
            case 2:
                i.setBackgroundResource(R.drawable.b3);
                return i;
            case 3:
                i.setBackgroundResource(R.drawable.b4);
                return i;
            case 4:
                i.setBackgroundResource(R.drawable.b5);
                return i;
            default:
                i.setBackgroundColor(0);
                return i;
        }
    }

    public static EventCategory getCategoryById(int id) throws UnknownCategoryException {
        for (EventCategory c: ConfigurationProvider.eventCategories()){
            if (c.remote_id == id){
                return c;
            }
        }
        throw new UnknownCategoryException(id);
    }

    public static void redirectLogin(Context currentContext){
        Intent intent = new Intent(currentContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        currentContext.startActivity(intent);
    }

    public static void logout() {
        if (isLoggedIn()){
            auth.logout();
            RestClient.instance().logoutUser();
        }
    }

    public static Promise ready() {
        return deferred.promise();
    }

    public static void updateGoogleMessagingToken(Context context, String token) {
        Log.i(TAG, "Updating token for GCM: " + token);
        Call<RestFeedback> call = RestClient.service().updateGoogleMessagingToken(token);
        RestClient.buildCall(call)
                .onResponse(new HttpCallback() {
                    @Override
                    public void successful(Object feedback) {
                        Log.d(TAG, "Update token success");
                    }

                    @Override
                    public void notSuccessful() {
                        Log.e(TAG, "Update token fail");
                    }
                })
                .perform();
    }

    public static void requestGcmToken(Context context) {
        Log.d(TAG, "Starting IntentService to update user token");
        Intent intent = new Intent(context, RegistrationIntentService.class);
        //intent.putExtra(Constants.RECEIVER, mResultReceiver);
        //intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        context.startService(intent);
    }

}
