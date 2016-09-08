package com.timappweb.timapp.utils;


import android.support.test.espresso.ViewInteraction;

import com.timappweb.timapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 08/09/2016.
 *
 */
public class PickTagsForm {

    private final RecyclerViewHelper suggestedTagRV;
    private final ViewInteraction submitBtn;

    public PickTagsForm() {
        suggestedTagRV = new RecyclerViewHelper(R.id.rv_search_suggested_tags);
        submitBtn = onView(withId(R.id.action_post));
    }

    public PickTagsForm pick(int position) {
        suggestedTagRV
                .clickItem(position);
        return this;
    }

    public void submit() {
        submitBtn
                .perform(click());
    }
}
