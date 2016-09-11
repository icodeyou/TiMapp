package com.timappweb.timapp.activities;

import android.content.Intent;
import android.location.Location;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.dummy.DummyEventFactory;
import com.timappweb.timapp.fixtures.MockLocation;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.MockLocationProvider;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.idlingresource.ApiCallIdlingResource;
import com.timappweb.timapp.utils.viewinteraction.PickTagsForm;
import com.timappweb.timapp.utils.location.LocationManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Stephane on 17/08/2016.
 *
 * @warning User must be already logged in to perform this test suite
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddEventTagActivityTest {

    public static final int EVENT_ID = 562;

    @Rule
    public ActivityTestRule<AddTagActivity> mActivityRule = new ActivityTestRule<>(
            AddTagActivity.class, false, false);
    private ApiCallIdlingResource apiCallIdlingResource;
    private MockLocationProvider mockLocationProvider;

    @Before
    public void setUp() throws Exception {
        Event dummyEvent = DummyEventFactory.create();
        dummyEvent = (Event) dummyEvent.mySave();

        Intent intent = IntentsUtils.buildIntentAddTags(MyApplication.getApplicationBaseContext(), dummyEvent);
        mActivityRule.launchActivity(intent);

        mockLocationProvider = MockLocationProvider.createGPSProvider(mActivityRule.getActivity());
        Location lastLocation = mockLocationProvider.pushLocation(MockLocation.START_TEST);
        LocationManager.setLastLocation(lastLocation);
        apiCallIdlingResource = new ApiCallIdlingResource();
        Espresso.registerIdlingResources(apiCallIdlingResource);
    }

    @After
    public void unregisterIntentServiceIdlingResource() {
        Espresso.unregisterIdlingResources(apiCallIdlingResource);
    }

    @Test
    public void pickTags() {
        TestUtil.sleep(3000);
        new PickTagsForm()
            .pick(0)
            .pick(3)
            .pick(5)
            .submit();

        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }
}
