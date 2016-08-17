package com.timappweb.timapp;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.activities.AddEventActivity;
import com.timappweb.timapp.activities.ListFriendsActivity;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.dummy.DummyUserFactory;

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
    public void postNewEvent() {
        // Type text and then press the button.
        String mStringToBetyped = "My event";

        ViewInteraction inputName = onView(withId(R.id.event_name));
        inputName
                .perform(clearText())
                .perform(typeText(mStringToBetyped), closeSoftKeyboard());

        onView(withId(R.id.description_edit_text))
                .perform(clearText())
                .perform(typeText(mStringToBetyped), closeSoftKeyboard());

        onView(withId(R.id.action_post))
                .perform(click());

        // Check that the text was changed.
        //onView(withId(R.id.textToBeChanged))
          //      .check(matches(withText(mStringToBetyped)));
    }


}
