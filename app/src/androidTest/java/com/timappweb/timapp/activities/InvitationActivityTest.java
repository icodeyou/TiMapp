package com.timappweb.timapp.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.InvitationsActivity;
import com.timappweb.timapp.activities.ListFriendsActivity;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.dummy.DummyUserFactory;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.RecyclerViewHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Stephane on 17/08/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class InvitationActivityTest {

    @Rule
    public ActivityTestRule<InvitationsActivity> mActivityRule = new ActivityTestRule<>(
            InvitationsActivity.class);

    @Before
    public void initUserSession() {
        assertTrue(MyApplication.isLoggedIn());
    }

    @Test
    public void testViewProfile() {
        new RecyclerViewHelper(R.id.rv_friends)
                .clickItem(0);
        ActivityHelper.assertCurrentActivity(ProfileActivity.class);
    }



}
