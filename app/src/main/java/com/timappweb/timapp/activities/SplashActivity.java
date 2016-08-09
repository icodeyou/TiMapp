package com.timappweb.timapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.rest.managers.MultipleHttpCallManager;

/**
 * Created by stephane on 3/26/2016.
 */
public class SplashActivity extends BaseActivity {

    private static final String TAG = "SplashActivity";
    private static final String CALL_ID_TOKEN = "check_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Activity context = this;


        final MultipleHttpCallManager manager = ConfigurationProvider.load(this);

        if (MyApplication.isLoggedIn()) {
            HttpCallManager tokenCallManager = MyApplication.auth.checkToken();
            if (tokenCallManager != null) {
                manager.addCall(CALL_ID_TOKEN, tokenCallManager);
            }
        }

        manager
                .setCallback(new MultipleHttpCallManager.Callback() {
                    @Override
                    public void onPostExecute() {
                        if (!ConfigurationProvider.hasFullConfiguration()){
                            Log.e(TAG, "Cannot load server configuration");
                            IntentsUtils.fatalError(SplashActivity.this, R.string.fatal_error_no_network_title, R.string.fatal_error_no_network_message);
                        }
                        else{
                            if (manager.isSuccess(ConfigurationProvider.CALL_ID_SPOT_CATEGORIES)
                                    && manager.isSuccess(ConfigurationProvider.CALL_ID_APPLICATION_RULES)
                                    && manager.isSuccess(ConfigurationProvider.CALL_ID_EVENT_CATEGORIES)) {
                                ConfigurationProvider.updateLastUpdateTime();
                            }

                            if (MyApplication.isLoggedIn()){
                                IntentsUtils.home(SplashActivity.this);
                            }
                            else{
                                IntentsUtils.login(SplashActivity.this);
                            }
                        }
                        finish();
                    }

                    @Override
                    public void onAllSuccess() {
                    }
                })
                .perform();

    }

}
