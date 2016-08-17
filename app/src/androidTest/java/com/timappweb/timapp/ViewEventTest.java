package com.timappweb.timapp;

import android.app.Activity;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.activities.LoginActivity;
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
public class ViewEventTest {

    public static final int EVENT_ID = 1;

    @Rule
    public ActivityTestRule<EventActivity> mActivityRule = new ActivityTestRule<>(EventActivity.class, false, false);


    @Before
    public void startActivity(){
        Intent intent = IntentsUtils.buildIntentViewPlace(MyApplication.getApplicationBaseContext(), EVENT_ID);
        mActivityRule.launchActivity(intent);
    }


    @Test
    public void testNavigate() {

    }
}
