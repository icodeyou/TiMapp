package com.timappweb.timapp.utils.viewinteraction;

import android.support.test.espresso.ViewInteraction;

import com.timappweb.timapp.R;
import com.timappweb.timapp.utils.TestUtil;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 07/09/2016.
 */
public class AddSpotForm extends FormHelper {
    private final ViewInteraction inputName;
    private final CategorySelectorHelper categorySelector;
    private final RecyclerViewHelper spotRV;
    private ViewInteraction submitBtn;

    public AddSpotForm() {
        inputName = onView(withId(R.id.name_spot));
        categorySelector = new CategorySelectorHelper();
        spotRV = new RecyclerViewHelper(R.id.spots_rv);
        submitBtn = onView(withId(R.id.action_create));
    }

    public AddSpotForm setName(String value) {
        typeIn(inputName, value);
        return this;
    }

    public AddSpotForm setCategory(int position) {
        categorySelector.selectByPosition(position);
        return this;
    }

    public AddSpotForm submit() {
        submitBtn
                .perform(click());
        return this;
    }

    public AddSpotForm tryAll(String name) {
        this.submit()
                .setName("Test")
                .submit()
                .setCategory(2)
                .setName("")
                .submit()
                .setName(name);
        return this;
    }

    public AddSpotForm selectExistingSpot(int position) {
        spotRV
                .clickItem(position);
        return this;
    }

    public AddSpotForm waitForExistingSpotLoad() {
        TestUtil.sleep(2000);
        return this;
    }

    public RecyclerViewHelper getExistingSpotList() {
        return spotRV;
    }

    public void checkNameEquals(String s) {
        checkFieldValue(inputName, s);
    }

}
