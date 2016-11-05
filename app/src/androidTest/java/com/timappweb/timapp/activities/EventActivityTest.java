package com.timappweb.timapp.activities;

import android.content.Intent;
import android.location.Location;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.fixtures.EventsFixture;
import com.timappweb.timapp.fixtures.MockLocation;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.annotations.CreateAuthAction;
import com.timappweb.timapp.utils.annotations.CreateConfigAction;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.mocklocations.AbstractMockLocationProvider;
import com.timappweb.timapp.utils.mocklocations.MockLocationProvider;
import com.timappweb.timapp.utils.viewinteraction.RecyclerViewHelper;
import com.timappweb.timapp.utils.viewinteraction.StatusButtonHelper;
import com.timappweb.timapp.utils.viewinteraction.ViewEventHelper;

import org.junit.After;
import org.junit.Before;
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
public class EventActivityTest extends AbstractActivityTest{

    private static final String TAG = "EventActivityTest";

    @Rule
    public ActivityTestRule<EventActivity> mActivityRule = new ActivityTestRule<>(EventActivity.class, false, false);

    private ViewEventHelper viewEventHelper;
    private StatusButtonHelper statusButtonHelper;

    @Before
    public void startActivity(){
        //this.systemAnimations(false);
        this.idlingApiCall();

        super.beforeTest();

        viewEventHelper = new ViewEventHelper();
        statusButtonHelper = new StatusButtonHelper();
        this.getMockLocationProvider().route(new AbstractMockLocationProvider.MockLocationRoute() {
            @Override
            public Location getNextLocation() {
                Log.v(TAG, "Creating new mock location from route");
                return AbstractMockLocationProvider.createMockLocation("MockedLocation", MockLocation.START_TEST.latitude, MockLocation.START_TEST.longitude);
            }
        }, 2000);
    }

    @After
    public void after(){
        this.resetAsBeforeTest();
    }

    // ---------------------------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------------------------

    @Test
    @CreateAuthAction(replaceIfExists = false)
    @CreateConfigAction
    public void testAddPicture() {
        Intent intent = IntentsUtils.buildIntentViewEvent(MyApplication.getApplicationBaseContext(), EventsFixture.getRegularEvent());
        mActivityRule.launchActivity(intent);

        this.waitForFineLocation(mActivityRule);
        viewEventHelper.addPicture();
        // TODO take the picture
    }

    @Test
    @CreateAuthAction(replaceIfExists = false)
    @CreateConfigAction
    public void testAddTags() {
        Intent intent = IntentsUtils.buildIntentViewEvent(MyApplication.getApplicationBaseContext(), EventsFixture.getRegularEvent());
        mActivityRule.launchActivity(intent);

        this.waitForFineLocation(mActivityRule);
        viewEventHelper.addTags();
        TestUtil.sleep(1000);
        ActivityHelper.assertCurrentActivity(AddTagActivity.class);
    }

    @Test
    @CreateAuthAction(replaceIfExists = false)
    @CreateConfigAction
    public void testAddPeople() {
        Intent intent = IntentsUtils.buildIntentViewEvent(MyApplication.getApplicationBaseContext(), EventsFixture.getRegularEvent());
        mActivityRule.launchActivity(intent);

        viewEventHelper.invitePeople();
        ActivityHelper.assertCurrentActivity(InviteFriendsActivity.class);

        TestUtil.sleep(5000);
        new RecyclerViewHelper(R.id.rv_friends)
                .clickItem(0)
                .clickItem(1);

        onView(withId(R.id.action_invite))
                .perform(click());

        ActivityHelper.assertCurrentActivity(EventActivity.class);

        viewEventHelper.swipeToPeopleTab();

        // TODO check that count has been updated
    }

    @Test
    @CreateConfigAction
    public void testViewPicture() {
        Intent intent = IntentsUtils.buildIntentViewEvent(MyApplication.getApplicationBaseContext(), EventsFixture.getRegularEvent());
        mActivityRule.launchActivity(intent);

        viewEventHelper.viewPicture(0);
        TestUtil.sleep(1000);
        ActivityHelper.assertCurrentActivity(EventPicturesActivity.class);
        ActivityHelper.goBack();
        TestUtil.sleep(1000);
        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }

    @Test
    @CreateConfigAction
    public void startNavigation() {
        Intent intent = IntentsUtils.buildIntentViewEvent(MyApplication.getApplicationBaseContext(), EventsFixture.getRegularEvent());
        mActivityRule.launchActivity(intent);

        viewEventHelper.startNavigation();
    }

    @Test
    @CreateAuthAction (payloadId = "107557263042633") //TODO : Make sure that the user doesn't have any status (clear data base ?)
    public void testHereButton() {
        Intent intent = IntentsUtils.buildIntentViewEvent(MyApplication.getApplicationBaseContext(), EventsFixture.getRegularEvent());
        mActivityRule.launchActivity(intent);

        setMockLocationAround(true, mActivityRule.getActivity().getEvent());
        statusButtonHelper.assertHereButton();

        statusButtonHelper.changeStatus(true);
        statusButtonHelper.assertButtonActivation(true);

        statusButtonHelper.changeStatus(false);
        statusButtonHelper.assertButtonActivation(false);
    }

    @Test
    @CreateAuthAction (payloadId = "107557263042633")
    public void testComingButton() {
        Intent intent = IntentsUtils.buildIntentViewEvent(MyApplication.getApplicationBaseContext(), EventsFixture.getRegularEvent());
        mActivityRule.launchActivity(intent);

        setMockLocationAround(true, mActivityRule.getActivity().getEvent());
        statusButtonHelper.assertComingButton();

        statusButtonHelper.changeStatus(true);
        statusButtonHelper.assertButtonActivation(true);

        statusButtonHelper.changeStatus(false);
        statusButtonHelper.assertButtonActivation(false);
    }

    //TODO : test with users that already have a status !

    @Test
    @CreateAuthAction
    public void testNoLocationButton() {
        Intent intent = IntentsUtils.buildIntentViewEvent(MyApplication.getApplicationBaseContext(), EventsFixture.getRegularEvent());
        mActivityRule.launchActivity(intent);

        setMockLocationAround(false, mActivityRule.getActivity().getEvent());
        statusButtonHelper.assertNoLocationView();

        setMockLocationAway(false);
        statusButtonHelper.assertNoLocationView();

        LocationManager.clearLastLocation();
        statusButtonHelper.assertNoLocationView();
    }

    @Test
    @CreateAuthAction
    public void testOverView() {
        Intent intent = IntentsUtils.buildIntentViewEvent(MyApplication.getApplicationBaseContext(), EventsFixture.getOverEvent());
        mActivityRule.launchActivity(intent);

        statusButtonHelper.assertOverView();
    }

    @Test
    @CreateAuthAction
    public void testPlannedView() {
        Intent intent = IntentsUtils.buildIntentViewEvent(MyApplication.getApplicationBaseContext(), EventsFixture.getPlannedEvent());
        mActivityRule.launchActivity(intent);

        setMockLocationAround(true, mActivityRule.getActivity().getEvent());
        statusButtonHelper.assertComingButton();

        setMockLocationAway(true);
        statusButtonHelper.assertComingButton();

        setMockLocationAround(false,  mActivityRule.getActivity().getEvent());
        statusButtonHelper.assertComingButton();

        setMockLocationAway(false);
        statusButtonHelper.assertComingButton();
    }

    private void setMockLocationAround(boolean isFineLocation, Event event) {
        Location location = MockLocationProvider.createMockLocation(event.getPosition().latitude,event.getPosition().longitude);
        location.setAccuracy(isFineLocation ? 1 : 20000);
        this.getMockLocationProvider().pushLocation(location);

        TestUtil.sleep(2000);
    }

    private void setMockLocationAway(boolean isFineLocation) {
        Location location = MockLocationProvider.createMockLocation(10,15);
        location.setAccuracy(isFineLocation ? 1 : 20000);
        this.getMockLocationProvider().pushLocation(location);

        TestUtil.sleep(2000);
    }

}
