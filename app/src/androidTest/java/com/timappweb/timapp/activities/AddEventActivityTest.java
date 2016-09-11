package com.timappweb.timapp.activities;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Intent;
import android.support.test.espresso.AppNotIdleException;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.dummy.DummyEventFactory;
import com.timappweb.timapp.fixtures.MockLocation;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.DisableQuotaRequestInterceptor;
import com.timappweb.timapp.utils.MockLocationProvider;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.idlingresource.ApiCallIdlingResource;
import com.timappweb.timapp.utils.viewinteraction.AddEventForm;
import com.timappweb.timapp.utils.viewinteraction.AddSpotForm;
import com.timappweb.timapp.utils.idlingresource.ProgressIdlingResource;
import com.timappweb.timapp.utils.viewinteraction.ViewEventHelper;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Stephane on 17/08/2016.
 *
 * @warning User must be already logged in to perform this test suite
 *
 * TODO: disable quota on server and local side for tests to work properly
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddEventActivityTest{

    private ProgressIdlingResource progressIdlingResource;
    private MockLocationProvider mockLocationProvider;
    private ApiCallIdlingResource apiCallIdlingResource;


    @Rule
    public ActivityTestRule<AddEventActivity> mActivityRule = new ActivityTestRule<>(
            AddEventActivity.class, false, false);

    @Before
    public void setUp() throws Exception {
        apiCallIdlingResource = new ApiCallIdlingResource();
        Espresso.registerIdlingResources(apiCallIdlingResource);

        AddEventActivity activity = mActivityRule.getActivity();

        mockLocationProvider = MockLocationProvider.createGPSProvider(activity);
        mockLocationProvider.pushLocation(MockLocation.START_TEST);
        //progressIdlingResource = new ProgressIdlingResource(activity, activity.getProgressBar());
        //Espresso.registerIdlingResources(progressIdlingResource);
        assertTrue(MyApplication.isLoggedIn());
    }

    @After
    public void unregisterIntentServiceIdlingResource() {
        Espresso.unregisterIdlingResources(apiCallIdlingResource);
    }

    @Test
    public void postNewEventNoSpot() {
        this.disableQuota();

        String eventName = DummyEventFactory.uniqName();
        String eventDescription = "This is the big description for my event!";

        new AddEventForm()
                .setCategory(1)
                .setName(eventName)
                .setDescription(eventDescription)
                .submit();

        // TODO wait for load done..

        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }


    @Test
    public void postNewEventWithExistingSpot() throws InterruptedException {
        this.disableQuota();

        String eventName = DummyEventFactory.uniqName();
        String eventDescription = "This is the big description for my event!";

        AddEventForm addEventForm = new AddEventForm()
                .setName(eventName)
                .setDescription(eventDescription)
                .setCategory(1)
                .addSpot();

        new AddSpotForm()
                .waitForExistingSpotLoad()
                .selectExistingSpot(0);

        addEventForm
                .submit();

        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }

    @Test
    public void postNewEventWithNewSpot() {
        this.disableQuota();

        String eventName = DummyEventFactory.uniqName();
        String eventDescription = "This is the big description for my event!";
        String spotName = "My Spot";

        AddEventForm addEventForm = new AddEventForm()
                .setCategory(0)
                .setName(eventName)
                .setDescription(eventDescription)
                .addSpot();

        new AddSpotForm()
                .setName(spotName)
                .setCategory(0)
                .submit();

        addEventForm
                .submit();

        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }


    @Test
    public void postValidationErrors() {
        String eventName = "O";
        String eventDescription = "";

        AddEventForm addEventForm = new AddEventForm()
                .assertSubmitDisabled()
                .setCategory(0)
                .assertSubmitDisabled()
                .setName(eventName)
                .assertSubmitEnabled()
                .setDescription(eventDescription)
                .submit();

        ActivityHelper.assertCurrentActivity(AddEventActivity.class);
        addEventForm.assertNameError();
    }

    /***
     * Test that we can edit a spot newly created
     */
    @Test
    public void reditCreatedSpot() {
        String spotName = "My new spot unique";

        AddEventForm addEventForm = new AddEventForm()
                .addSpot();

        AddSpotForm addSpotForm = new AddSpotForm()
                .setName(spotName)
                .setCategory(1)
                .submit();

        addEventForm.editSpot();

        addSpotForm.checkNameEquals(spotName);
        // TODO check category too
    }

    @Test
    public void postExistingEvent() {
        // TODO
        String eventName = "Existing event";
        String eventDescription = "";

        AddEventForm addEventForm = new AddEventForm()
                .setCategory(0)
                .setName(eventName)
                .setDescription(eventDescription)
                .submit();

        ActivityHelper.assertCurrentActivity(AddEventActivity.class);
        addEventForm.assertNameError();
    }


    // TODO
    @Test
    public void noValidGpsLocation() {

    }
    // ---------------------------------------------------------------------------------------------

    private void disableQuota() {
        RestClient.instance().getHttpBuilder()
                .addInterceptor(new DisableQuotaRequestInterceptor());
        RestClient.instance().buildHttpClient();
    }
}
