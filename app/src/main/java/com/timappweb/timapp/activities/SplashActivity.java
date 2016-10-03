package com.timappweb.timapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.EventStatusManager;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.rest.managers.MultipleHttpCallManager;
import com.timappweb.timapp.views.RetryDialog;

/**
 * Created by stephane on 3/26/2016.
 */
public class SplashActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SplashActivity";
    private static final String CALL_ID_TOKEN = "check_token";
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.loadConfig();

        // Build GoogleApiClient with AppInvite API for receiving deep links
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .build();

        // Check if this app was launched from a deep link. Setting autoLaunchDeepLink to true
        // would automatically launch the deep link if one is found.
        boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(@NonNull AppInviteInvitationResult result) {
                                if (result.getStatus().isSuccess()) {
                                    // Extract deep link from Intent
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    Log.d(TAG, "Received deep link: " + deepLink);
                                    // Handle the deep link. For example, open the linked
                                    // content, or apply promotional credit to the user's
                                    // account.

                                } else {
                                    Log.d(TAG, "getInvitation: no deep link found.");
                                }
                            }
                        });
    }

    private void loadConfig() {

        final MultipleHttpCallManager callsManager = ConfigurationProvider.load(this);

        if (MyApplication.isLoggedIn()) {
            HttpCallManager tokenCallManager = MyApplication.auth.checkToken();
            if (tokenCallManager != null) {
                callsManager.addCall(CALL_ID_TOKEN, tokenCallManager);
            }
        }

        callsManager
                .setCallback(new MultipleHttpCallManager.Callback() {
                    @Override
                    public void onPostExecute() {
                        if (!ConfigurationProvider.hasFullConfiguration()){
                            Log.e(TAG, "Cannot load server configuration");
                            if (!ConfigurationProvider.hasRulesConfig()) {
                                Log.e(TAG, "    - No rules configuration");
                            }
                            if (!ConfigurationProvider.hasSpotCategoriesConfig()) {
                                Log.e(TAG, "    - No spot categories configuration");
                            }
                            if (!ConfigurationProvider.hasEventCategoriesConfig()) {
                                Log.e(TAG, "    - No event categories configuration");
                            }
                            ConfigurationProvider.clearStaticVariables();
                            RetryDialog.show(SplashActivity.this, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    SplashActivity.this.loadConfig();
                                }
                            }, getString(R.string.cannot_load_server_configuration_title),
                                    getString(R.string.cannot_load_server_configuration_msg));
                        }
                        else{
                            if (callsManager.isSuccess(ConfigurationProvider.CALL_ID_SPOT_CATEGORIES)
                                    && callsManager.isSuccess(ConfigurationProvider.CALL_ID_APPLICATION_RULES)
                                    && callsManager.isSuccess(ConfigurationProvider.CALL_ID_EVENT_CATEGORIES)) {
                                ConfigurationProvider.updateLastUpdateTime();
                            }

                            if (MyApplication.isLoggedIn()){
                                Event currentEvent = EventStatusManager.getCurrentEvent();
                                if (currentEvent != null && !currentEvent.isOver()){
                                    IntentsUtils.viewSpecifiedEvent(SplashActivity.this, currentEvent);
                                }
                                else{
                                    IntentsUtils.home(SplashActivity.this);
                                }
                            }
                            else if (MyApplication.isFirstLaunch()){
                                Log.i(TAG, "User starting app for the first time");
                                IntentsUtils.presentApp(SplashActivity.this);
                            }
                            else{
                                IntentsUtils.login(SplashActivity.this);
                            }
                            MyApplication.updateLastLaunch();
                            finish();
                        }
                    }

                    @Override
                    public void onAllSuccess() {
                    }
                })
                .perform();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
