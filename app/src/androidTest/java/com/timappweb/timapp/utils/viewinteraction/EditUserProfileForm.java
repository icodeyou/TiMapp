package com.timappweb.timapp.utils.viewinteraction;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.EspressoKey;
import android.view.inputmethod.EditorInfo;

import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.TestUtil;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by Stephane on 07/09/2016.
 */
public class EditUserProfileForm extends FormHelper {

    private final ViewInteraction inputName;
    private final RecyclerViewHelper tagsRV;
    private ViewInteraction submitBtn;

    public EditUserProfileForm() {
        inputName = onView(withId(R.id.edit_text));
        tagsRV = new RecyclerViewHelper(R.id.selected_tags_profile);
        submitBtn = onView(withId(R.id.button_submit));
    }

    public EditUserProfileForm addTag(String value) {
        typeIn(inputName, value)
                .perform(pressImeActionButton());
        return this;
    }

    public EditUserProfileForm submit() {
        TestUtil.sleep(250);
        submitBtn.perform(click());
        return this;
    }

    public EditUserProfileForm assertValid(){
        submitBtn
                .check(matches(isClickable()))
                .check(matches(isEnabled()));
        return this;
    }
}
