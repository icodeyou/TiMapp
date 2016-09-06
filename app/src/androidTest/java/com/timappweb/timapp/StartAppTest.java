package com.timappweb.timapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.activities.SplashActivity;
import com.timappweb.timapp.config.IntentsUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.runner.lifecycle.Stage.RESUMED;
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


    @Test
    public void testStartNoInternetButConfig() {
        // TODO
        setApplicationData();
        startActivity();
    }

    @Test
    public void testStartNoInternetNoConfig() {
        // TODO
        clearApplicationData();
        startActivity();
    }

    //----------------------------------------------------------------------------------------------

    @Test
    public void testStartInternetNoConfig() {
        // TODO
        clearApplicationData();
        startActivity();
    }

    @Test
    public void testStartInternetAndConfig() {
        // TODO
        setApplicationData();
        startActivity();
    }

    // ---------------------------------------------------------------------------------------------

    private void startActivity() {
        mActivityRule.launchActivity(new Intent());
    }

    private void clearApplicationData(){
        // TODO
        //((ActivityManager) MyApplication.getApplicationBaseContext().getSystemService(Context.ACTIVITY_SERVICE)).clearApplicationUserData();
    }

    private void setApplicationData() {
        // TODO
    }
}
