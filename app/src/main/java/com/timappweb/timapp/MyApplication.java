package com.timappweb.timapp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.activities.SplashActivity;
import com.timappweb.timapp.auth.AuthManager;
import com.timappweb.timapp.auth.AuthManagerFactory;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.data.entities.SearchFilter;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.io.responses.RestFeedback;
import com.timappweb.timapp.sync.AbstractSyncAdapter;
import com.timappweb.timapp.utils.ImagePipelineConfigFactory;
import com.timappweb.timapp.utils.KeyValueStorage;

import net.danlew.android.joda.JodaTimeAndroid;

import retrofit2.Call;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
    private static final String KEY_LAST_START = "last_app_start";


    public static SearchFilter searchFilter = new SearchFilter();
    public static AuthManager auth;
    private static Context _appContext;

    public static Context getApplicationBaseContext(){
        return _appContext;
    }

    public static AuthManager getAuthManager() {
        return auth;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ActiveAndroid.dispose();
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

    @Override
    public void onCreate(){
        super.onCreate();
        //this.deleteDatabase(getString(R.string.db_name));
        _appContext = getApplicationContext();
        ActiveAndroid.initialize(this);
        Fresco.initialize(this, ImagePipelineConfigFactory.getImagePipelineConfig(this));
        MyApplication.auth = AuthManagerFactory.create();
        RestClient.init(this, getResources().getString(R.string.api_base_url), MyApplication.getAuthManager());
        KeyValueStorage.init(this, RestClient.instance().getGson());
        JodaTimeAndroid.init(this);
        QuotaManager.init(getApplicationContext()); // TODO must be unitialized only for logged in users
        AbstractSyncAdapter.initializeSyncAdapter(this);
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

            /*
            mSimpleFacebook.logout(new OnLogoutListener() {
                @Override
                public void onLogout() {
                    Log.i(TAG, "You are logged out");
                }
            });*/

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

    public static boolean isFirstLaunch() {
        return KeyValueStorage.getSafeLong(KEY_LAST_START, 0L) == 0L;
    }

    public static void updateLastLaunch(){
        KeyValueStorage.in().putLong(KEY_LAST_START, System.currentTimeMillis()).commit();
    }

    public static void clearStoredData() {
        KeyValueStorage.in().clear().commit();
    }

    public static void restart(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK );
        context.startActivity(intent);
    }
}
