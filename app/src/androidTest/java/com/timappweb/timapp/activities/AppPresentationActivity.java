package com.timappweb.timapp.activities;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.timappweb.timapp.utils.viewinteraction.PresentationPager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

/**
 * Created by Stephane on 17/08/2016.
 *
 * @warning User must be already logged in to perform this test suite
 *
 * TODO: disable quota on server and local side for tests to work properly
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AppPresentationActivity extends AbstractActivityTest{

    private PresentationPager presentationPager;

    @Rule
    public ActivityTestRule<PresentationActivity> mActivityRule = new ActivityTestRule<>(
            PresentationActivity.class);

    @Before
    public void setUp() throws Exception {
        this.presentationPager = new PresentationPager();
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void navigate() {
        for (int i = 0; i < 6; i++){
            presentationPager.next();
        }
        presentationPager.go();
    }

    @Test
    public void skip() {
        presentationPager.skip();
    }

}
