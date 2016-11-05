package com.timappweb.timapp.activities;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.data.models.dummy.DummyTagFactory;
import com.timappweb.timapp.fixtures.UsersFixture;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.TestUtil;
import com.timappweb.timapp.utils.annotations.ClearAuth;
import com.timappweb.timapp.utils.annotations.ClearDBTable;
import com.timappweb.timapp.utils.annotations.CreateAuthAction;
import com.timappweb.timapp.utils.annotations.CreateConfigAction;
import com.timappweb.timapp.utils.viewinteraction.EditUserProfileForm;
import com.timappweb.timapp.utils.viewinteraction.ProfileHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * Created by Stephane on 07/09/2016.
 */
public class ProfileActivityTest extends AbstractActivityTest{

    private final int TAG_NUMBER = 3;
    private Intent intent;

    @Rule
    public ActivityTestRule<ProfileActivity> mActivityRule = new ActivityTestRule<>(
            ProfileActivity.class, false, false);

    @Before
    public void setUp() throws Exception {
        this.beforeTest();
        intent = new Intent(MyApplication.getApplicationBaseContext(), ProfileActivity.class);
    }

    @After
    public void tearDown() throws Exception {
        this.resetAsBeforeTest();
    }
    // ---------------------------------------------------------------------------------------------

    @Test
    @CreateAuthAction
    @CreateConfigAction
    public void testEditProfile() {
        mActivityRule.launchActivity(intent);

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
        TestUtil.sleep(3000);
        ActivityHelper.assertCurrentActivity(ProfileActivity.class);
        profileHelper.assertCountTags(TAG_NUMBER);
    }

    @Test
    @CreateAuthAction
    @CreateConfigAction
    @Ignore
    public void testInvalidTags(){
        mActivityRule.launchActivity(intent);

        ActivityHelper.btnClick(R.id.action_edit_profile);
        ActivityHelper.assertCurrentActivity(EditProfileActivity.class);
        ProfileHelper profileHelper = new ProfileHelper();
        EditUserProfileForm editProfileForm = new EditUserProfileForm();
        editProfileForm.addTag(DummyTagFactory.invalidName());

        // TODO [Jack] finish test
    }

    @Test
    @ClearAuth
    @CreateConfigAction
    @ClearDBTable(models = {Tag.class})
    public void testViewOtherProfile() {
        intent.putExtra(IntentsUtils.KEY_USER_ID, UsersFixture.userIdWithTags());
        mActivityRule.launchActivity(intent);

        // See profile
        ActivityHelper.assertCurrentActivity(ProfileActivity.class);
        TestUtil.sleep(3000);//
        ProfileHelper profileHelper = new ProfileHelper();
        profileHelper.assertName("No Friends"); // TODO cst

        profileHelper.assertCountTags(TAG_NUMBER);
    }
}
