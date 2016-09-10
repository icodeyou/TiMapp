package com.timappweb.timapp;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.activities.SplashActivity;
import com.timappweb.timapp.config.ConfigurationProvider;
import com.timappweb.timapp.utils.ActivityHelper;

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
public class StartAppTest {

    @Rule
    public ActivityTestRule<SplashActivity> mActivityRule =
            new ActivityTestRule<>(SplashActivity.class, false, false);


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
    //----------------------------------------------------------------------------------------------

    @Test
    public void testStartInternetNoConfig() {
        clearConfigurationData();
        startActivity();

        // TODO wait for config load...
        ActivityHelper.assertCurrentActivity(DrawerActivity.class);
    }

    @Test
    public void testStartInternetAndConfig() {
        setApplicationData();
        startActivity();

        // TODO wait for config load...
        ActivityHelper.assertCurrentActivity(DrawerActivity.class);
    }

    // ---------------------------------------------------------------------------------------------

    private void startActivity() {
        mActivityRule.launchActivity(new Intent());
    }

    private void clearConfigurationData(){
        ConfigurationProvider.clearAll();
    }

    private void setApplicationData() {
        // TODO
    }
}
