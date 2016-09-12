package com.timappweb.timapp.utils.viewinteraction;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.v7.widget.RecyclerView;

import com.timappweb.timapp.utils.ActivityHelper;
import com.timappweb.timapp.utils.assertions.RecyclerViewItemCountAssertion;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Created by Stephane on 08/09/2016.
 */
public class RecyclerViewHelper {

    private final ViewInteraction viewInteractionRV;
    private RecyclerView recyclerView;
    private final int id;

    public RecyclerViewHelper(int id) {
        viewInteractionRV = onView(withId(id));
        this.id = id;
    }

    public RecyclerViewHelper clickItem(int position) {
        this.scrollToPosition(position);
        viewInteractionRV
                .perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));

        return this;
    }

    public RecyclerViewHelper scrollToBottom() {
        this.scrollToPosition(this.getItemCount());
        return this;
    }

    public RecyclerViewHelper scrollToPosition(int position){
        viewInteractionRV
                .check(matches(isDisplayed()))
                .check(new RecyclerViewItemCountAssertion(greaterThan(position)))
                .perform(RecyclerViewActions.scrollToPosition(position));
        return this;
    }

    public int getItemCount() {
        if (getRecyclerView() == null){
            return 0;
        }
        return getRecyclerView().getAdapter().getItemCount();
    }

    public RecyclerViewHelper checkItemCount(int count){
        viewInteractionRV
                .check(new RecyclerViewItemCountAssertion(count));
        return this;
    }

    public RecyclerViewHelper checkItemCount(Matcher<Integer> matcher){
        viewInteractionRV
                .check(new RecyclerViewItemCountAssertion(matcher));
        return this;
    }

    public RecyclerViewHelper checkLoadingMore() {
        // TODO
        return this;
    }

    public RecyclerView getRecyclerView() {
        if (ActivityHelper.getActivityInstance() != null){
            recyclerView = (RecyclerView) ActivityHelper.getActivityInstance().findViewById(id);
        }
        return recyclerView;
    }

    public RecyclerViewHelper scrollUp() {
        // TODO
        return this;
    }
}
