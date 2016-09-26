package com.timappweb.timapp.activities;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.annotations.ClearAuth;
import com.timappweb.timapp.utils.annotations.ClearConfig;
import com.timappweb.timapp.utils.annotations.ClearFirstStart;
import com.timappweb.timapp.utils.annotations.CreateAuthAction;
import com.timappweb.timapp.utils.annotations.CreateConfigAction;
import com.timappweb.timapp.utils.annotations.CreateLastLaunch;
import com.timappweb.timapp.utils.idlingresource.ApiCallIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Stephane on 17/08/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SplashActivityTest extends AbstractActivityTest {


    @Rule
    public ActivityTestRule<SplashActivity> mActivityRule =
            new ActivityTestRule<>(SplashActivity.class, false, false);


    @Before
    public void setUp() throws Exception {
        this.idlingApiCall();
        this.systemAnimations(false);
        super.beforeTest();
        startActivity();
    }

    @After
    public void tearDown() throws Exception {
        this.resetAsBeforeTest();
    }


    //----------------------------------------------------------------------------------------------
    /*
    @Test
    public void testStartNoInternetNoConfig() {
        // TODO

        clearConfigurationData();
        startActivity();
    }


    @Test
    public void testStartNoInternetButConfig() {
        // TODO
        setApplicationData();
        startActivity();
    }*/

    @Test
    @ClearConfig
    @ClearFirstStart
    public void testFirstStart() {
        TestUtil.sleep(5000);
        ActivityHelper.assertCurrentActivity(PresentationActivity.class);
    }

    @Test
    @ClearAuth
    @ClearConfig
    @CreateLastLaunch
    public void testStartInternetNoConfig() {
        TestUtil.sleep(5000);
        ActivityHelper.assertCurrentActivity(LoginActivity.class);
    }

    @Test
    @CreateConfigAction
    @ClearAuth
    @CreateLastLaunch
    public void testStartInternetAndConfig() {
        TestUtil.sleep(5000);
        ActivityHelper.assertCurrentActivity(LoginActivity.class);
    }

    // ---------------------------------------------------------------------------------------------

    private void startActivity() {
        mActivityRule.launchActivity(new Intent(MyApplication.getApplicationBaseContext(), SplashActivity.class));
    }

}
