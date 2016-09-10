package com.timappweb.timapp.activities;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.data.models.dummy.DummyEventFactory;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.viewinteraction.AddEventForm;
import com.timappweb.timapp.utils.viewinteraction.AddSpotForm;
import com.timappweb.timapp.utils.idlingresource.ProgressIdlingResource;

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

    @Rule
    public ActivityTestRule<AddEventActivity> mActivityRule = new ActivityTestRule<>(
            AddEventActivity.class);

    private ProgressIdlingResource progressIdlingResource;

    @Before
    public void setUp() throws Exception {
        AddEventActivity activity = (AddEventActivity) ActivityHelper.getActivityInstance();
        //progressIdlingResource = new ProgressIdlingResource(activity, activity.getProgressBar());
        //Espresso.registerIdlingResources(progressIdlingResource);
        assertTrue(MyApplication.isLoggedIn());
    }

    @Test
    public void postNewEventNoSpot() {
        String eventName = DummyEventFactory.uniqName();
        String eventDescription = "This is the big description for my event!";

        new AddEventForm()
                .setCategory(1)
                .setName(eventName)
                .setDescription(eventDescription)
                .submit();

        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }

    @Test
    public void postNewEventWithExistingSpot() throws InterruptedException {
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
        // TODO
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
}
