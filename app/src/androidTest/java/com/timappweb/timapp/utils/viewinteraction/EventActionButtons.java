package com.timappweb.timapp.utils.viewinteraction;

import android.support.test.espresso.ViewInteraction;

import com.timappweb.timapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 17/08/2016.
 */
public class EventActionButtons {

    private final ViewInteraction mMainBtn;
    private boolean mIsOpen = false;

    public EventActionButtons() {
        mMainBtn = onView(withId(R.id.multiple_actions));
    }

    public void camera(){
        if (!mIsOpen){
            toggle();
        }
        actionClick(R.id.action_camera);
    }

    public void tags(){
        if (!mIsOpen){
            toggle();
        }
        actionClick(R.id.action_tag);
    }

    public void invite(){
        if (!mIsOpen){
            toggle();
        }
        actionClick(R.id.action_invite);
    }

    public void toggle(){
        mMainBtn.perform(click());
        this.mIsOpen = !this.mIsOpen;
    }

    // ---------------------------------------------------------------------------------------------

    private void actionClick(int id) {
        onView(withId(id))
                //.check(matches(isDisplayed()))
                //.check(matches(isEnabled()))
                .perform(click());
    }

}
