package com.timappweb.timapp.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.activities.InvitationsActivity;
import com.timappweb.timapp.activities.ListFriendsActivity;
import com.timappweb.timapp.data.models.User;
import com.timappweb.timapp.data.models.dummy.DummyUserFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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
        User user = DummyUserFactory.create();
        MyApplication.login(user, "", "");
    }

    @Test
    public void checkList() {
        // Type text and then press the button.

        // Check that the text was changed.
        //onView(withId(R.id.textToBeChanged))
          //      .check(matches(withText(mStringToBetyped)));
    }


}
