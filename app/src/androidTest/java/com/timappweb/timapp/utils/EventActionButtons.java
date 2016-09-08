package com.timappweb.timapp.utils;

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

    public static void camera(){
        actionClick(R.id.action_camera);
    }

    public static  void tags(){
        actionClick(R.id.action_tag);
    }

    public static  void invite(){
        actionClick(R.id.action_invite);
    }

    public static  void toggle(){
        actionClick(R.id.multiple_actions);
    }

    // ---------------------------------------------------------------------------------------------

    private static void actionClick(int id) {
        ActivityHelper.btnClick(id);
    }

}
