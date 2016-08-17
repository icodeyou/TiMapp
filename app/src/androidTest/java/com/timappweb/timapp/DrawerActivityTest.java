package com.timappweb.timapp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.activities.ListFriendsActivity;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.dummy.DummyUserFactory;

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
public class DrawerActivityTest {

    @Rule
    public ActivityTestRule<DrawerActivity> mActivityRule = new ActivityTestRule<>(
            DrawerActivity.class);

    @Before
    public void initUserSession() {

    }

    @Test
    public void clickOnMenu() {
        onView(withId(R.id.action_list))
                .perform(click());

    }
    @Test
    public void selectMapEvent() {

    }

    @Test
    public void clickOnEvent() {
        onView(withId(R.id.fab_button_add_event))
                .perform(click());

    }

    @Test
    public void showEventList() {
        onView(withId(R.id.action_list))
                .perform(click());
    }




}
