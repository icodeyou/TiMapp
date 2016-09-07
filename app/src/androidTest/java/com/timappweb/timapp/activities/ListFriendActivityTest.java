package com.timappweb.timapp.activities;

import android.content.Intent;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.activities.AddEventActivity;
import com.timappweb.timapp.activities.ListFriendsActivity;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.dummy.DummyUserFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Stephane on 17/08/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ListFriendActivityTest {

    @Rule
    public ActivityTestRule<ListFriendsActivity> mActivityRule = new ActivityTestRule<>(
            ListFriendsActivity.class, false, false);

    @Before
    public void startActivity(){
        User user = DummyUserFactory.create();
        boolean isLoggedIn = MyApplication.login(user, "", "");
        assertTrue(isLoggedIn);
        Intent intent = new Intent(MyApplication.getApplicationBaseContext(), ListFriendsActivity.class);
        mActivityRule.launchActivity(intent);
    }

    @Test
    public void checkList() {
        // Type text and then press the button.

        // Check that the text was changed.
        //onView(withId(R.id.textToBeChanged))
          //      .check(matches(withText(mStringToBetyped)));
    }


}
