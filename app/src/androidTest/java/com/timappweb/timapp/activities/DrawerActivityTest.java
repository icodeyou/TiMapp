package com.timappweb.timapp.activities;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.activities.ListFriendsActivity;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.dummy.DummyUserFactory;
import com.timappweb.timapp.utils.viewinteraction.ExploreHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 17/08/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@Ignore
public class DrawerActivityTest extends AbstractActivityTest{

    @Rule
    public ActivityTestRule<DrawerActivity> mActivityRule = new ActivityTestRule<>(
            DrawerActivity.class);
    private ExploreHelper exploreHelper;

    @Before
    public void beforeTest() {
        exploreHelper = new ExploreHelper();
        super.beforeTest();
    }

    @After
    public void afterTest() {
        super.resetAsBeforeTest();
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testOpenDrawer(){
        exploreHelper.openDrawer();

        // TODO close it
    }

    // TODO tests each links
}
