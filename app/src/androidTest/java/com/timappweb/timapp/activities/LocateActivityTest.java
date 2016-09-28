package com.timappweb.timapp.activities;

import android.support.test.rule.ActivityTestRule;
import android.test.suitebuilder.annotation.LargeTest;

import android.support.test.runner.AndroidJUnit4;
import com.timappweb.timapp.R;
import com.timappweb.timapp.fixtures.MockLocation;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.annotations.CreateAuthAction;
import com.timappweb.timapp.utils.annotations.CreateConfigAction;
import com.timappweb.timapp.utils.viewinteraction.RecyclerViewHelper;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
/**
 * Created by Stephane on 17/08/2016.
 *
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocateActivityTest extends AbstractActivityTest{

    @Rule
    public ActivityTestRule<LocateActivity> mActivityRule = new ActivityTestRule<>(
            LocateActivity.class);

    private RecyclerViewHelper eventRV;


    @Before
    public void setUp() throws Exception {
        this.idlingApiCall();
        this.systemAnimations(false);
        super.beforeTest();
        eventRV = new RecyclerViewHelper(R.id.list_events);
        this.getMockLocationProvider().pushLocation(MockLocation.START_TEST);
    }

    @After
    public void tearDown() throws Exception {
        this.resetAsBeforeTest();
    }


    @Test
    @CreateAuthAction
    @CreateConfigAction
    public void testExistingEvent() {
        this.getMockLocationProvider().pushLocation(MockLocation.MANY_SPOTS);
        eventRV.checkItemCount(3);
    }

    @Test
    @CreateAuthAction
    @CreateConfigAction
    public void testNoExistingEvent() {
        this.getMockLocationProvider().pushLocation(MockLocation.NO_SPOT);
        TestUtil.sleep(3000);
        ActivityHelper.assertCurrentActivity(AddEventActivity.class);
    }


    @Test
    @CreateAuthAction
    @CreateConfigAction
    public void testLocationChanged() {
        this.getMockLocationProvider().pushLocation(MockLocation.MANY_SPOTS);
        eventRV.checkItemCount(greaterThanOrEqualTo(1));
        this.getMockLocationProvider().pushLocation(MockLocation.NO_SPOT);
        eventRV.checkItemCount(0);
    }

}
