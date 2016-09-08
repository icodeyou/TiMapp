package com.timappweb.timapp.activities;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.MockLocationProvider;
import com.timappweb.timapp.utils.PickTagsForm;
import com.timappweb.timapp.utils.location.LocationManager;

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

    @Before
    public void setUp() throws Exception {
        Intent intent = IntentsUtils.buildIntentViewPlace(MyApplication.getApplicationBaseContext(), EVENT_ID);
        mActivityRule.launchActivity(intent);
        //mockLocationProvider = new MockLocationProvider(android.location.LocationManager.NETWORK_PROVIDER, ActivityHelper.getActivityInstance());
        //mockLocationProvider = new MockLocationProvider(android.location.LocationManager.GPS_PROVIDER, ActivityHelper.getActivityInstance());

        LocationManager.setLastLocation(MockLocationProvider.create(android.location.LocationManager.GPS_PROVIDER, 0, 5));
    }

    @Test
    public void pickTags() {
        new PickTagsForm()
            .pick(0)
            .pick(3)
            .pick(5)
            .submit();

        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }
}
