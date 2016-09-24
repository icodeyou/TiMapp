package com.timappweb.timapp.utils.viewinteraction;

import android.support.test.espresso.ViewInteraction;

import com.timappweb.timapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 23/09/2016.
 */
public class PresentationPager {

    private final ViewInteraction nextBtn;
    private final ViewInteraction skipBtn;
    private final ViewInteraction goBtn;

    public PresentationPager() {
        nextBtn = onView(withId(R.id.next_button));
        skipBtn = onView(withId(R.id.skip_button));
        goBtn = onView(withId(R.id.final_button));
    }

    public PresentationPager next(){
        nextBtn.perform(click());
        return this;
    }

    public PresentationPager skip(){
        skipBtn.perform(click());
        return this;

    }
    public PresentationPager go(){
        goBtn.perform(click());
        return this;
    }
}
