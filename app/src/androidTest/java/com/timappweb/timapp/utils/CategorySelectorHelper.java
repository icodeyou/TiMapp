package com.timappweb.timapp.utils;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.timappweb.timapp.R;
import com.timappweb.timapp.views.CategorySelectorView;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 07/09/2016.
 */
public class CategorySelectorHelper {

    private final ViewInteraction mainCategories;
    private final ViewInteraction allCategories;
    private final ViewInteraction openCloseBtn;

    public CategorySelectorHelper() {
        mainCategories = onView(withId(R.id.rv_main_categories));
        allCategories = onView(withId(R.id.rv_all_categories));
        openCloseBtn = onView(withId(R.id.more_button));
    }

    public void selectByPosition(int position) {
        if (position >= CategorySelectorView.NUMBER_OF_MAIN_CATEGORIES){
            this.open();
            allCategories
                    .perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
        }
        else{
            mainCategories
                    .perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
        }
    }
    public void open(){
        openCloseBtn.perform(click());
    }
    public void close(){
        openCloseBtn.perform(click());
    }
}
