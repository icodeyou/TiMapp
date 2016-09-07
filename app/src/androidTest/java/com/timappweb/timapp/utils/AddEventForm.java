package com.timappweb.timapp.utils;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.AddSpotActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 07/09/2016.
 */
public class AddEventForm  extends TestForm{
    private final ViewInteraction inputEventName;
    private final ViewInteraction inputEventDescription;
    private final ViewInteraction submitButton;

    public AddEventForm() {
        inputEventName = onView(withId(R.id.event_name));
        inputEventDescription = onView(withId(R.id.description_edit_text));
        submitButton = onView(withId(R.id.action_post));
    }

    public AddEventForm setName(String value) {
        typeIn(inputEventName, value);
        return this;
    }

    public AddEventForm setDescription(String value) {
        typeIn(inputEventDescription, value);
        return this;
    }


    public AddEventForm setCategory(int position) {
        onView(withId(R.id.category_selector))
                .perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
        return this;
    }

    public AddEventForm submit() {
        submitButton
                .perform(click());
        return this;
    }

    public AddEventForm addSpot() {
        onView(withId(R.id.button_add_spot))
                .perform(click());
        ActivityHelper.assertCurrentActivity(AddSpotActivity.class);
        return this;
    }

    public AddEventForm assertNameError() {
        this.assertValidationError(inputEventName);
        return this;
    }
}
