package com.timappweb.timapp.activities;

import android.app.Activity;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.test.suitebuilder.annotation.LargeTest;

import com.facebook.FacebookActivity;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.activities.ListFriendsActivity;
import com.timappweb.timapp.activities.LoginActivity;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.annotations.AuthState;
import com.timappweb.timapp.utils.annotations.CreateConfigAction;

import static android.support.test.runner.lifecycle.Stage.RESUMED;


import org.junit.After;
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
public class LoginActivityTest extends AbstractActivityTest{

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

    @Before
    public void setUp() throws Exception {
        this.systemAnimations(false);
        this.idlingApiCall();
    }

    @After
    public void tearDown() throws Exception {
        this.resetAsBeforeTest();
    }

    @Test
    @AuthState(logging = AuthState.LoginState.NO)
    @CreateConfigAction
    public void testSkip() {
        onView(ViewMatchers.withId(R.id.skip_loggin_button))
                .perform(click());

        ActivityHelper.assertCurrentActivity(DrawerActivity.class);
    }

    @Test
    @AuthState(logging = AuthState.LoginState.NO)
    @CreateConfigAction
    public void testLogin() {
        onView(withId(R.id.facebook_login_button))
                .perform(click());

        ActivityHelper.assertCurrentActivity(FacebookActivity.class);

        // TODO add interaction with facebook login
        // THEN
        // ActivityHelper.assertCurrentActivity(DrawerActivity.class);
    }

}
