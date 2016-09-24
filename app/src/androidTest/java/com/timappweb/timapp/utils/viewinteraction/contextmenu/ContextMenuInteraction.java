package com.timappweb.timapp.utils.viewinteraction.contextmenu;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.Root;

import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.TestUtil;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by Stephane on 23/09/2016.
 */
public class ContextMenuInteraction {

    public static void clickOn(int id){
        TestUtil.sleep(500);
        //onView(withId(id))
        //        .inRoot(withDecorView(not(is(ActivityHelper.getActivityInstance().getWindow().getDecorView()))))
        //        .perform(click());
        onView(withId(id))
                .inRoot(isContextualMenu())
                .perform(click());
    }

    public static Matcher<Root> isPopupWindow() {
        return isPlatformPopup();
    }

    /**
     * Matches {@link Root}s that are popups - like autocomplete suggestions or the actionbar spinner.
     */
    public static Matcher<Root> isContextualMenu() {
        return new TypeSafeMatcher<Root>() {
            @Override
            public boolean matchesSafely(Root item) {
                return withDecorView(withClassName(Matchers.containsString("ContextMenu"))).matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with decor view of type *ContextMenu*");
            }
        };
    }

}
