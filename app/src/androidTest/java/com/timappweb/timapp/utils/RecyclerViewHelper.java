package com.timappweb.timapp.utils;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.timappweb.timapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 08/09/2016.
 */
public class RecyclerViewHelper {

    private final ViewInteraction rv;

    public RecyclerViewHelper(int id) {
        rv = onView(withId(id));
    }

    public RecyclerViewHelper clickItem(int position) {
        onView(withId(R.id.rv_friends))
                .check(matches(isDisplayed()))
                .perform(RecyclerViewActions.scrollToPosition(position))
                .perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));

        return this;
    }
}
