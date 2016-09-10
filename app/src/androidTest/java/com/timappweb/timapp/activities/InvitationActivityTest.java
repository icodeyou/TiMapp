package com.timappweb.timapp.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.viewinteraction.RecyclerViewHelper;

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
    public void setUp() {
        assertTrue(MyApplication.isLoggedIn());
    }

    @Test
    public void testViewEvent() {
        new RecyclerViewHelper(R.id.rv_invitations)
                .clickItem(0);
        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }


}
