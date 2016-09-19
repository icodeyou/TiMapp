package com.timappweb.timapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.config.AuthProvider;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.data.entities.SearchFilter;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.io.responses.RestFeedback;
import com.timappweb.timapp.services.RegistrationIntentService;
import com.timappweb.timapp.sync.AbstractSyncAdapter;
import com.timappweb.timapp.utils.ImagePipelineConfigFactory;
import com.timappweb.timapp.utils.KeyValueStorage;

import net.danlew.android.joda.JodaTimeAndroid;

import retrofit2.Call;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class MyApplication extends com.activeandroid.app.Application {

    private static final String TAG = "MyApplication";
    private static final String KEY_LAST_START = "last_app_start";


    public static SearchFilter searchFilter = new SearchFilter();
    public static AuthProvider auth;
    private static Context _appContext;

    public static Context getApplicationBaseContext(){
        return _appContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //MultiDex.install(this);
    }


    /**
     * @return true if user is logged in
     */
    public static boolean isLoggedIn(){
        return auth.isLoggedIn();
    }

    public static boolean isCurrentUser(int userId){
        return MyApplication.isLoggedIn() && MyApplication.getCurrentUser().remote_id == userId;
    }
    public static boolean isCurrentUser(User mUser) {
        return MyApplication.isCurrentUser(mUser.remote_id);
    }

    public static User getCurrentUser(){
        return auth.getCurrentUser();
    }

    public static boolean login(Context context, User user, String token, String accessToken){
        if (auth.login(user, token, accessToken)){
            return true;
        }
        return false;
    }
    public static boolean login(User user, String token, String accessToken){
        return login(MyApplication.getApplicationBaseContext(), user, token, accessToken);
    }

    @Override
    public void onCreate(){
        super.onCreate();

        //this.deleteDatabase(getString(R.string.db_name));

        _appContext = getApplicationContext();

        Fresco.initialize(this, ImagePipelineConfigFactory.getImagePipelineConfig(this));
        MyApplication.auth = new AuthProvider();
        RestClient.init(this, getResources().getString(R.string.api_base_url), MyApplication.auth);
        KeyValueStorage.init(this, RestClient.instance().getGson());
        initFacebookPermissions(); // Useless because friend are loaded from the server ... ? TODO move from here
        JodaTimeAndroid.init(this);
        QuotaManager.init(getApplicationContext()); // TODO must be unitialized only for logged in users
        AbstractSyncAdapter.initializeSyncAdapter(this);
    }


    private void initFacebookPermissions() {
        Permission[] permissions = new Permission[] {
                Permission.USER_PHOTOS,
                Permission.EMAIL,
                Permission.USER_FRIENDS,
        };
        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId(getResources().getString(R.string.facebook_app_id))
                .setNamespace(getResources().getString(R.string.namespace))
                .setPermissions(permissions)
                .build();
        SimpleFacebook.setConfiguration(configuration);
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

    public static boolean isFirstLaunch() {
        return KeyValueStorage.getSafeLong(KEY_LAST_START, 0L) == 0L;
    }

    public static void updateLastLaunch(){
        KeyValueStorage.in().putLong(KEY_LAST_START, System.currentTimeMillis()).commit();
    }

    public static void clearStoredData() {
        KeyValueStorage.in().clear().commit();
    }
}
