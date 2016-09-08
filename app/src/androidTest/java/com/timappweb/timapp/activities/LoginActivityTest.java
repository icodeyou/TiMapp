package com.timappweb.timapp.activities;

import android.app.Activity;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.activities.ListFriendsActivity;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.utils.ActivityHelper;

import static android.support.test.runner.lifecycle.Stage.RESUMED;


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
import static junit.framework.Assert.assertTrue;

/**
 * Created by Stephane on 17/08/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);


    @Test
    public void testSkip() {
        onView(ViewMatchers.withId(R.id.skip_loggin_button))
                .perform(click());

        ActivityHelper.assertCurrentActivity(DrawerActivity.class);
    }
    @Test
    public void testLogin() {
        onView(withId(R.id.facebook_login_button))
                .perform(click());

        // TODO wait for facebook..
        ActivityHelper.assertCurrentActivity(DrawerActivity.class);
    }

}
