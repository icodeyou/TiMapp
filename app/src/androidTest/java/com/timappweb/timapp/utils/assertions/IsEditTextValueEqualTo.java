package com.timappweb.timapp.utils.assertions;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Stephane on 10/09/2016.
 */
public class IsEditTextValueEqualTo implements ViewAssertion{

    private final String expectedValue;

    public IsEditTextValueEqualTo(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
        if (noViewFoundException != null) {
            throw noViewFoundException;
        }

        assertTrue((view instanceof TextView) || (view instanceof EditText));

        if (view != null) {
            String text;
            if (view instanceof TextView) {
                text =((TextView) view).getText().toString();
            } else {
                text =((EditText) view).getText().toString();
            }

            assertTrue(text.equals(expectedValue));
        }
    }
}