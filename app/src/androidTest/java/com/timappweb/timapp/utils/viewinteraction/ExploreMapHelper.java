package com.timappweb.timapp.utils.viewinteraction;

import android.support.test.espresso.ViewInteraction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.timappweb.timapp.R;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 09/09/2016.
 */
public class ExploreMapHelper {

    private final UiDevice device;
    private final ViewInteraction eventPreviewContainer;

    public ExploreMapHelper() {
        device = UiDevice.getInstance(getInstrumentation());
        eventPreviewContainer = onView(withId(R.id.event_view_container));
    }


    public ExploreMapHelper clickOnMarker(String title) throws UiObjectNotFoundException {
        UiObject marker = device.findObject(new UiSelector().descriptionContains(title));
        marker.click();
        return this;
    }

    public void clickOnEventPreview() {
        eventPreviewContainer
                .check(matches(isDisplayed()))
                .check(matches(isClickable()))
                .perform(click());
    }

    /**
     * TODO implement
     * @return
     */
    public ExploreMapHelper clickOnCluster() {
        throw new RuntimeException("Not implemented yet");
        //return this;
    }
}
