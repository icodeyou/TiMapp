package com.timappweb.timapp.activities;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.idlingresource.ApiCallIdlingResource;
import com.timappweb.timapp.utils.viewinteraction.RecyclerViewHelper;
import com.timappweb.timapp.utils.viewinteraction.ViewEventHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
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
    private ViewEventHelper viewEventHelper;

    @Before
    public void startActivity(){
        //Espresso.registerIdlingResources(new ApiCallIdlingResource());

        Intent intent = IntentsUtils.buildIntentViewPlace(MyApplication.getApplicationBaseContext(), EVENT_ID);
        mActivityRule.launchActivity(intent);

        viewEventHelper = new ViewEventHelper();
    }

    @After
    public void after(){

    }

    @Test
    public void testAddPicture() {
        viewEventHelper.addPicture();
    }

    @Test
    public void testAddTags() {
        viewEventHelper.addTags();
    }
    @Test
    public void testAddPeople() {
        viewEventHelper.invitePeople();
        ActivityHelper.assertCurrentActivity(InviteFriendsActivity.class);

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
    public void testViewPicture() {
        viewEventHelper.viewPicture(0);

        ActivityHelper.assertCurrentActivity(EventPicturesActivity.class);

        ActivityHelper.goBack();

        ActivityHelper.assertCurrentActivity(EventPicturesActivity.class);
    }
    @Test
    public void startNavigation() {
        viewEventHelper.startNavigation();
    }
}
