package com.timappweb.timapp.activities;

import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.core.deps.guava.util.concurrent.Runnables;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.fixtures.MockLocation;
import com.timappweb.timapp.fragments.ExploreMapFragment;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.annotations.CreateAuthAction;
import com.timappweb.timapp.utils.annotations.CreateConfigAction;
import com.timappweb.timapp.utils.idlingresource.ApiCallIdlingResource;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.mocklocations.AbstractMockLocationProvider;
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
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Created by Stephane on 17/08/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExploreActivityTest extends AbstractActivityTest {

    private static final String TAG = "ExploreActivityTest";
    private ExploreHelper exploreHelper;

    @Rule
    public ActivityTestRule<DrawerActivity> mActivityRule = new ActivityTestRule<>(
            DrawerActivity.class, false, false);


    @Before
    public void setUp() throws Exception {
        this.idlingApiCall();
        this.systemAnimations(false);

        Location fakeLocation = AbstractMockLocationProvider.createMockLocation("MockedLocation", MockLocation.START_TEST.latitude, MockLocation.START_TEST.longitude);

        LocationManager.setLastLocation(fakeLocation);

        Intent mapIntent = new Intent(MyApplication.getApplicationBaseContext(), DrawerActivity.class);

        mActivityRule.launchActivity(mapIntent);
        exploreHelper = new ExploreHelper();
        super.beforeTest();
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
        centerMapOnLocation(MockLocation.START_TEST);
        // TODO
        exploreHelper
                .getMap()
                .clickOnMarker("Concert improv")
                .clickOnEventPreview();
    }

    @Test
    @CreateConfigAction
    public void testClickOnEventInList() {
        centerMapOnLocation(MockLocation.START_TEST);
        TestUtil.sleep(3000);
        exploreHelper
                .openList()
                .getListEvent()
                .checkItemCount(greaterThan(0))
                .clickItem(0);
        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }

    @Test
    @CreateConfigAction
    @CreateAuthAction
    public void testAddEvent() {
        this.getMockLocationProvider().pushLocation(MockLocation.START_TEST);
        this.waitForFineLocation(mActivityRule);
        QuotaManager.clear();
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
        this.getMockLocationProvider().pushLocation(MockLocation.START_TEST);
        this.waitForFineLocation(mActivityRule);
        QuotaManager.clear();
        exploreHelper.addEvent();
        ActivityHelper.assertCurrentActivity(LocateActivity.class);
        new RecyclerViewHelper(R.id.list_events)
                .clickItem(0);
        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }

    @Test
    @CreateConfigAction
    public void testShowEventList() {
        centerMapOnLocation(MockLocation.START_TEST);
        exploreHelper.openList();
    }



    private void centerMapOnLocation(final LatLng latLng) {
        final ExploreMapFragment exploreMapFragment = mActivityRule.getActivity().getExploreMapFragment();
        assertNotNull("Cannot center map if explore fragment is null", exploreMapFragment);

        // We must run the code on the main thread
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //exploreMapFragment.centerMap(latLng, null);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)      // Sets latitude and longitude
                        .zoom(16.0f)  // Sets the zoom
                        .build();                     // Creates a CameraPosition from the builder
                exploreMapFragment.getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        };
        TestUtil.runOnMainThread(exploreMapFragment.getContext(), runnable);
    }
}
