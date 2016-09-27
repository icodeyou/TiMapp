package com.timappweb.timapp.activities;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.R;
import com.timappweb.timapp.fixtures.MockLocation;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.annotations.CreateAuthAction;
import com.timappweb.timapp.utils.annotations.CreateConfigAction;
import com.timappweb.timapp.utils.idlingresource.ApiCallIdlingResource;
import com.timappweb.timapp.utils.viewinteraction.ExploreHelper;
import com.timappweb.timapp.utils.mocklocations.MockLocationProvider;
import com.timappweb.timapp.utils.viewinteraction.RecyclerViewHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 17/08/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExploreActivityTest extends AbstractActivityTest {

    private ExploreHelper exploreHelper;
    private ApiCallIdlingResource apiCallIdlingResource;

    @Rule
    public ActivityTestRule<DrawerActivity> mActivityRule = new ActivityTestRule<>(
            DrawerActivity.class);


    @Before
    public void setUp() throws Exception {
        this.idlingApiCall();
        this.systemAnimations(false);
        super.beforeTest();

        exploreHelper = new ExploreHelper();
        this.getMockLocationProvider().pushLocation(MockLocation.START_TEST);
    }

    @After
    public void tearDown() throws Exception {
        this.resetAsBeforeTest();
    }

    // ---------------------------------------------------------------------------------------------


    @Test
    @CreateConfigAction
    public void testClickOnMenu() {
        exploreHelper.openDrawer();
    }

    @Test
    @CreateConfigAction
    @Ignore
    public void testClickOnEventOnMap() throws UiObjectNotFoundException {
        // TODO
        exploreHelper
                .getMap()
                .clickOnMarker("Concert improv")
                .clickOnEventPreview();
    }

    @Test
    @CreateConfigAction
    public void testClickOnEventInList() {
        TestUtil.sleep(3000);
        exploreHelper
                .openList()
                .getListEvent()
                .clickItem(0);
        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }

    @Test
    @CreateConfigAction
    @CreateAuthAction
    public void testAddEvent() {
        exploreHelper
                .addEvent();
        ActivityHelper.assertCurrentActivity(LocateActivity.class);
        ActivityHelper.btnClick(R.id.action_skip);
        ActivityHelper.assertCurrentActivity(AddEventActivity.class);
    }

    @Test
    @CreateConfigAction
    @CreateAuthAction
    public void testTryAddEventClickExistingEvent() {
        exploreHelper.addEvent();
        ActivityHelper.assertCurrentActivity(LocateActivity.class);
        new RecyclerViewHelper(R.id.list_events)
                .clickItem(0);
        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }

    @Test
    @CreateConfigAction
    public void testShowEventList() {
        exploreHelper.openList();
    }


}
