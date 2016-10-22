package com.timappweb.timapp.activities;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.fixtures.MockLocation;
import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.annotations.CreateAuthAction;
import com.timappweb.timapp.utils.annotations.CreateConfigAction;
import com.timappweb.timapp.utils.idlingresource.ApiCallIdlingResource;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.mocklocations.MockLocationProvider;
import com.timappweb.timapp.utils.viewinteraction.RecyclerViewHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

import static org.hamcrest.Matchers.greaterThan;
/**
 * Created by Stephane on 17/08/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class InvitationActivityTest extends AbstractActivityTest{

    private ApiCallIdlingResource apiCallIdlingResource;

    @Rule
    public ActivityTestRule<InvitationsActivity> mActivityRule = new ActivityTestRule<>(
            InvitationsActivity.class, false, false);


    @Before
    public void setUp(){
        this.idlingApiCall();
        this.systemAnimations(false);
        mActivityRule.launchActivity(new Intent(MyApplication.getApplicationBaseContext(), InvitationsActivity.class));

        // We need a gps location to hide the progress bar that simpleMessage distance to the user
        this.getMockLocationProvider().pushLocation(MockLocation.START_TEST);

        super.beforeTest();
    }

    @After
    public void unregisterIntentServiceIdlingResource() {
        super.resetAsBeforeTest();
    }

    @Test
    @CreateAuthAction
    @CreateConfigAction
    public void testViewEvent() {
        new RecyclerViewHelper(R.id.rv_invitations)
                .clickItem(0);
        ActivityHelper.assertCurrentActivity(EventActivity.class);
    }

    @Test
    @Ignore
    @CreateAuthAction
    @CreateConfigAction
    public void testViewEventOver() {
        // TODO
        new RecyclerViewHelper(R.id.rv_invitations)
                .checkItemCount(InvitationsActivity.LOCAL_LOAD_LIMIT)
                .clickItem(InvitationsActivity.LOCAL_LOAD_LIMIT - 1);
    }


    @Test
    @CreateAuthAction
    @CreateConfigAction
    public void testLoadMore() {
        new RecyclerViewHelper(R.id.rv_invitations)
                .checkItemCount(InvitationsActivity.LOCAL_LOAD_LIMIT)
                .scrollToPosition(InvitationsActivity.LOCAL_LOAD_LIMIT-1)
                .checkItemCount(greaterThan(InvitationsActivity.LOCAL_LOAD_LIMIT));
    }



    @Test
    @Ignore
    public void testRefresh() {
        // TODO
        new RecyclerViewHelper(R.id.rv_invitations)
                .scrollUp();
    }


}
