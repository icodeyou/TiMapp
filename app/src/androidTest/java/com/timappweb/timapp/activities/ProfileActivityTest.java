package com.timappweb.timapp.activities;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.viewinteraction.EditUserProfileForm;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Stephane on 07/09/2016.
 */
public class ProfileActivityTest extends AbstractActivityTest{

    private static final int PROFILE_ID = 1;
    @Rule
    public ActivityTestRule<ProfileActivity> mActivityRule = new ActivityTestRule<>(
            ProfileActivity.class, false, false);

    @Before
    public void setUp() throws Exception {
        this.systemAnimations(false);
    }

    @After
    public void tearDown() throws Exception {
        this.resetAsBeforeTest();
    }

    @Before
    public void startActivity(){
        Intent intent = IntentsUtils.buildIntentViewPlace(MyApplication.getApplicationBaseContext(), PROFILE_ID);
        mActivityRule.launchActivity(intent);
        assertTrue(MyApplication.isLoggedIn());
    }

    @Test
    public void testEditProfile() {
        ActivityHelper
                .btnClick(R.id.action_edit_profile);

        ActivityHelper.assertCurrentActivity(EditProfileActivity.class);

        new EditUserProfileForm() // TODO dynamic tag number
                .addTag("One")
                .addTag("Two")
                .addTag("Three")
                .assertValid()
                .submit();
        TestUtil.sleep(2000);
        ActivityHelper.assertCurrentActivity(ProfileActivity.class);
    }


    // TODO test view other user profile
    @Test
    public void testViewOtherProfile() {
        // Test load correctly
        // Test cannot edit tags
    }
}
