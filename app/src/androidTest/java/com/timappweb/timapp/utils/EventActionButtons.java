package com.timappweb.timapp.utils;

import com.timappweb.timapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 17/08/2016.
 */
public class EventActionButtons {

    public static void camera(){
        onView(withId(R.id.action_camera))
                .perform(click());
    }

    public static  void tags(){
        onView(withId(R.id.action_tag))
                .perform(click());
    }

    public static  void invite(){
        onView(withId(R.id.action_invite))
                .perform(click());
    }

    public static  void toggle(){
        onView(withId(R.id.multiple_actions))
                .perform(click());
    }
}
