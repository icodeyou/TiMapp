package com.timappweb.timapp.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.utils.viewinteraction.AddSpotForm;
import com.timappweb.timapp.utils.mocklocations.MockLocationProvider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Stephane on 17/08/2016.
 *
 * @warning User must be already logged in to perform this test suite
 *
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddSpotActivityTest {

    private AddSpotForm addSpotForm;
    private MockLocationProvider mockLocation;

    @Rule
    public ActivityTestRule<AddSpotActivity> mActivityRule = new ActivityTestRule<>(
            AddSpotActivity.class);

    @Before
    public void setUp() throws Exception {
        assertTrue(MyApplication.isLoggedIn());
        addSpotForm = new AddSpotForm();

        mockLocation = MockLocationProvider.createGPSProvider(mActivityRule.getActivity());
        //mockLocation.pushLocation(MockLocation.START_TEST);
    }

    @Test
    public void testExistingSpots() {
        addSpotForm
                .getExistingSpotList()
                .checkItemCount(3) // TODO
                .scrollToBottom()
                .checkLoadingMore();
    }

    @Test
    public void createNewSpot(){
        addSpotForm
                .assertSubmitDisabled()
                .setName("New name")
                .assertSubmitDisabled()
                .setCategory(1)
                .assertSubmitEnabled();
    }

    @Test
    public void testFilterSpot() {

    }

    @Test
    public void testNoUpToDateLocation() {

    }
}
