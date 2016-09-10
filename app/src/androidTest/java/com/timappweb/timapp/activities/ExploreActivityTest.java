package com.timappweb.timapp.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.R;
import com.timappweb.timapp.fixtures.MockLocation;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.viewinteraction.ExploreHelper;
import com.timappweb.timapp.utils.MockLocationProvider;
import com.timappweb.timapp.utils.viewinteraction.RecyclerViewHelper;

import org.junit.Before;
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
public class ExploreActivityTest {

    private ExploreHelper exploreHelper;
    private MockLocationProvider mockLocation;

    @Rule
    public ActivityTestRule<DrawerActivity> mActivityRule = new ActivityTestRule<>(
            DrawerActivity.class);


    @Before
    public void initUserSession() {
        exploreHelper = new ExploreHelper();
        mockLocation = MockLocationProvider.createGPSProvider();
        mockLocation.pushLocation(MockLocation.START_TEST);
    }

    @Test
    public void testClickOnMenu() {
        exploreHelper.openDrawer();
    }

    @Test
    public void testClickOnEventOnMap() throws UiObjectNotFoundException {
        exploreHelper
                .getMap()
                .clickOnMarker("Event ")
                .clickOnEventPreview();
    }


    @Test
    public void testClickOnEventInList() {
        exploreHelper
                .openList()
                .getListEvent()
                .clickItem(0);
        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }

    @Test
    public void testAddEvent() {
        exploreHelper
                .addEvent();
        ActivityHelper.assertCurrentActivity(LocateActivity.class);
        ActivityHelper.btnClick(R.id.action_skip);
        ActivityHelper.assertCurrentActivity(AddEventActivity.class);
    }

    @Test
    public void testTryAddEventClickExistingEvent() {
        exploreHelper.addEvent();
        ActivityHelper.assertCurrentActivity(LocateActivity.class);
        new RecyclerViewHelper(R.id.list_events)
                .clickItem(0);
        ActivityHelper.assertCurrentActivity(AddEventActivity.class);
    }

    @Test
    public void testShowEventList() {
        exploreHelper.openList();
    }


}
