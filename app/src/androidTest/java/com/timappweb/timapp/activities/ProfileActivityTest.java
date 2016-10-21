package com.timappweb.timapp.activities;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.dummy.DummyEventFactory;
import com.timappweb.timapp.data.models.dummy.DummyTagFactory;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.annotations.AuthState;
import com.timappweb.timapp.utils.annotations.CreateAuthAction;
import com.timappweb.timapp.utils.annotations.CreateConfigAction;
import com.timappweb.timapp.utils.viewinteraction.EditUserProfileForm;
import com.timappweb.timapp.utils.viewinteraction.ProfileHelper;

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
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    @CreateAuthAction
    @CreateConfigAction
    public void testEditProfile() {
        int TAG_NUMBER = 3;
        ActivityHelper.btnClick(R.id.action_edit_profile);

        ActivityHelper.assertCurrentActivity(EditProfileActivity.class);
        ProfileHelper profileHelper = new ProfileHelper();

        EditUserProfileForm editProfileForm = new EditUserProfileForm();
        for (int i = 0; i < TAG_NUMBER; i++){
            editProfileForm.addTag(DummyTagFactory.uniqName());
        }
        editProfileForm
                .assertValid()
                .submit();
        //TestUtil.sleep(2000);
        ActivityHelper.assertCurrentActivity(ProfileActivity.class);
        profileHelper.assertCountTags(TAG_NUMBER);
    }


    // TODO test view other user profile
    @Test
    @AuthState(logging = AuthState.LoginState.NO)
    @CreateConfigAction
    public void testViewOtherProfile() {
        // Test load correctly
        // Test cannot edit tags

        // TEST refresh
    }
}
