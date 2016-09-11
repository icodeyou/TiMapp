package com.timappweb.timapp.utils.viewinteraction;

import android.support.test.espresso.ViewInteraction;

import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.assertions.IsEditTextValueEqualTo;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static java.util.regex.Pattern.matches;

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

    public void checkFieldValue(ViewInteraction viewInteraction, String content) {
        viewInteraction
                .check(new IsEditTextValueEqualTo(content));
    }
}
