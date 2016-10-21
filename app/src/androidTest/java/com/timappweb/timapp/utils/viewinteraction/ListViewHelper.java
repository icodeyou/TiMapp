package com.timappweb.timapp.utils.viewinteraction;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.widget.ListView;

import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.assertions.RecyclerViewItemCountAssertion;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Created by Stephane on 08/09/2016.
 */
public class ListViewHelper<ItemType> {

    private final Class<ItemType> clazz;
    private ListView listView;
    private final int id;

    public ListViewHelper(int id, Class<ItemType> clazz) {
        this.id = id;
        this.clazz = clazz;
    }

    public ListViewHelper<ItemType> checkItemCount(int count){
        onData(instanceOf(clazz))
                .inAdapterView(allOf(withId(this.id), isDisplayed()))
                .atPosition(count-1)
                .check(matches(isDisplayed()));
        return this;
    }


    public ListView getListView() {
        if (ActivityHelper.getActivityInstance() != null){
            listView = (ListView) ActivityHelper.getActivityInstance().findViewById(id);
        }
        return listView;
    }

    public ListViewHelper<ItemType> checkWithText(String value) {
        onView(allOf(withText(value), withParent(withId(this.id))));
        return this;
    }
}
