package com.timappweb.timapp.utils.viewinteraction;

import android.support.test.espresso.ViewInteraction;

import com.timappweb.timapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Jack on 04/11/2016.
 */

public class StatusButtonHelper {
    private ViewInteraction disabledButton;
    private ViewInteraction activatedButton;
    private ViewInteraction statusText;

    public StatusButtonHelper() {
        disabledButton = onView(withId(R.id.status_button_disabled));
        activatedButton = onView(withId(R.id.status_button_activated));
        statusText = onView(withId(R.id.status_text));
    }

    public void changeStatus(boolean activate) {
        if(activate) {
            disabledButton.perform(click());
        }
        else {
            activatedButton.perform(click());
        }
    }

    public void assertButtonActivation(boolean activation) {
        if(activation) {
            activatedButton.check(matches(isDisplayed()));
        }
        else {
            disabledButton.check(matches(isDisplayed()));
        }
    }

    public void assertComingButton() {
        statusText.check(matches(withText(R.string.text_status_coming)));
    }

    public void assertHereButton() {
        statusText.check(matches(withText(R.string.text_status_here)));
    }

    public void assertNoLocationView() {
        onView(withId(R.id.no_location_view)).check(matches(isDisplayed()));
    }

    public void assertOverView() {
        onView(withId(R.id.over_view)).check(matches(isDisplayed()));
    }
}
