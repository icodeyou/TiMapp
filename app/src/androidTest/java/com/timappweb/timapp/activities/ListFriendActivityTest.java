package com.timappweb.timapp.activities;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.dummy.DummyUserFactory;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.RecyclerViewHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.action.ViewActions.click;
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
        Intent intent = new Intent(MyApplication.getApplicationBaseContext(), ListFriendsActivity.class);
        mActivityRule.launchActivity(intent);
        assertTrue(MyApplication.isLoggedIn());
    }

    @Test
    public void testViewUserProfile() {
        new RecyclerViewHelper(R.id.rv_friends)
                .clickItem(0);
        ActivityHelper.assertCurrentActivity(ProfileActivity.class);
    }

}
