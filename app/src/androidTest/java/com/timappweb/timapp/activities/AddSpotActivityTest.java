package com.timappweb.timapp.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.fixtures.MockLocation;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.annotations.CreateAuthAction;
import com.timappweb.timapp.utils.annotations.CreateConfigAction;
import com.timappweb.timapp.utils.mocklocations.MockLocationProvider;
import com.timappweb.timapp.utils.viewinteraction.AddSpotForm;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Created by Stephane on 17/08/2016.
 *
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddSpotActivityTest extends AbstractActivityTest{

    private AddSpotForm addSpotForm;
    private MockLocationProvider mockLocation;

    @Rule
    public ActivityTestRule<AddSpotActivity> mActivityRule = new ActivityTestRule<>(
            AddSpotActivity.class);

    @Before
    public void setUp() throws Exception {
        addSpotForm = new AddSpotForm();
        super.beforeTest();
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    @CreateConfigAction
    @CreateAuthAction
    public void testExistingSpots() {
        this.getMockLocationProvider().pushLocation(MockLocation.START_TEST);
        TestUtil.sleep(5000);
        addSpotForm
                .getExistingSpotList()
                .checkItemCount(greaterThanOrEqualTo(1));
                //.scrollToBottom();
    }

    @Test
    @CreateConfigAction
    @CreateAuthAction
    public void createNewSpot(){
        this.getMockLocationProvider().pushLocation(MockLocation.START_TEST);
        addSpotForm
                .submit()
                .setCategory(1)
                .submit()
                .setName("New name")
                .submit()
                .setCategory(2)
                .submit();
    }

    @Test
    @CreateConfigAction
    @CreateAuthAction
    @Ignore
    public void testFilterSpot() {
        // TODO
    }

    @Test
    @CreateConfigAction
    @CreateAuthAction
    @Ignore
    public void testNoUpToDateLocation() {
        // TODO
    }
}
