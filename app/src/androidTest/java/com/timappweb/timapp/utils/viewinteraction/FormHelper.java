package com.timappweb.timapp.utils.viewinteraction;

import android.support.test.espresso.ViewInteraction;

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
public class FormHelper {

    protected static void typeIn(ViewInteraction inputName, String content){
        inputName
                .perform(clearText())
                .perform(typeText(content), closeSoftKeyboard());
    }

    public void assertValidationError(ViewInteraction inputEventName) {
        // TODO
    }
}
