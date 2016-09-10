package com.timappweb.timapp.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.google.android.gms.maps.model.LatLng;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.fixtures.MockLocation;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.MockLocationProvider;
import com.timappweb.timapp.utils.viewinteraction.AddSpotForm;
import com.timappweb.timapp.utils.viewinteraction.RecyclerViewHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Stephane on 17/08/2016.
 *
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocateActivityTest {

    private MockLocationProvider mockLocation;

    @Rule
    public ActivityTestRule<LocateActivity> mActivityRule = new ActivityTestRule<>(
            LocateActivity.class);

    private RecyclerViewHelper eventRV;

    @Before
    public void setUp() throws Exception {
        assertTrue(MyApplication.isLoggedIn());
        eventRV = new RecyclerViewHelper(R.id.list_events);
        mockLocation = MockLocationProvider.createGPSProvider();
        mockLocation.pushLocation(MockLocation.START_TEST);
    }

    @Test
    public void testExistingEvent() {
        mockLocation.pushLocation(MockLocation.MANY_SPOTS);
        eventRV.checkItemCount(3);
    }

    @Test
    public void testNoExistingEvent() {
        mockLocation.pushLocation(MockLocation.NO_SPOT);
        ActivityHelper.assertCurrentActivity(AddEventActivity.class);
    }


    @Test
    public void testLocationChanged() {
        eventRV.checkItemCount(3);
        mockLocation.pushLocation(MockLocation.NO_SPOT);
        eventRV.checkItemCount(0);
    }

}
