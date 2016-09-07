package com.timappweb.timapp.activities;

import android.content.Intent;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.utils.EventActionButtons;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Stephane on 17/08/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ViewEventActivityTest {

    public static final int EVENT_ID = 562;

    @Rule
    public ActivityTestRule<EventActivity> mActivityRule = new ActivityTestRule<>(EventActivity.class, false, false);

    @Before
    public void startActivity(){
        Intent intent = IntentsUtils.buildIntentViewPlace(MyApplication.getApplicationBaseContext(), EVENT_ID);
        mActivityRule.launchActivity(intent);
    }

    @Test
    public void testAddPicture() {
        EventActionButtons.toggle();
        EventActionButtons.camera();
    }

    @Test
    public void testAddTags() {
        EventActionButtons.toggle();
        EventActionButtons.tags();
    }
    @Test
    public void testAddPeople() {
        EventActionButtons.toggle();
        EventActionButtons.invite();
    }

    @Test
    public void testViewPager(){
        onView(ViewMatchers.withId(R.id.event_viewpager))
                .perform(swipeRight());

        onView(withId(R.id.event_viewpager))
                .perform(swipeRight());
        // TODO assert
        onView(withId(R.id.event_viewpager))
                .perform(swipeRight());


        onView(withId(R.id.event_viewpager))
                .perform(swipeLeft());

        onView(withId(R.id.event_viewpager))
                .perform(swipeLeft());

        onView(withId(R.id.event_viewpager))
                .perform(swipeLeft());
        // Back to information
    }


    @Test
    public void startNavigation() {
        EventActionButtons.toggle();
        EventActionButtons.invite();
    }
}
