package com.timappweb.timapp.utils;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.timappweb.timapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 07/09/2016.
 */
public class AddSpotForm extends TestForm{
    private final ViewInteraction inputName;

    public AddSpotForm() {
        inputName = onView(withId(R.id.name_spot));
    }

    public AddSpotForm setName(String value) {
        typeIn(inputName, value);
        return this;
    }

    public AddSpotForm setCategory(int position) {
        onView(withId(R.id.category_selector))
                .perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
        return this;
    }

    public void submit() {
        onView(withId(R.id.action_create))
                .perform(click());
    }

}
