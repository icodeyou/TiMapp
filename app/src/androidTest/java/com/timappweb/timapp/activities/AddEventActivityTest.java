package com.timappweb.timapp.activities;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.activities.AddEventActivity;
import com.timappweb.timapp.activities.ListFriendsActivity;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.dummy.DummyUserFactory;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.AddEventForm;
import com.timappweb.timapp.utils.AddSpotForm;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 17/08/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddEventActivityTest {

    @Rule
    public ActivityTestRule<AddEventActivity> mActivityRule = new ActivityTestRule<>(
            AddEventActivity.class);

    @Before
    public void initUserSession() {
        User user = DummyUserFactory.create();
        MyApplication.login(user, "", "");
    }

    @Test
    public void postNewEventNoSpot() {
        String eventName = "My event";
        String eventDescription = "This is the big description for my event!";

        new AddEventForm()
                .setName(eventName)
                .setDescription(eventDescription)
                .submit();


        // Check that the text was changed.
        //onView(withId(R.id.textToBeChanged))
          //      .check(matches(withText(mStringToBetyped)));
    }

    @Test
    public void postNewEventWithSpot() {
        String eventName = "My event";
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

        addEventForm.submit();

        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }


    @Test
    public void postValidationErrors() {
        // TODO
        String eventName = "O";
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
