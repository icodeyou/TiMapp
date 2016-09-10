package com.timappweb.timapp.utils.viewinteraction;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;

import com.timappweb.timapp.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 09/09/2016.
 */
public class ExploreHelper {

    private final ViewInteraction actionDrawer;
    private final ViewInteraction addEventBtn;
    private final ViewInteraction actionEventListBtn;
    private final RecyclerViewHelper listEventRV;
    private final ExploreMapHelper map;
    private final ViewInteraction waitingForLocationLayout;

    public ExploreHelper() {
        actionDrawer = onView(ViewMatchers.withId(R.id.action_list));
        actionEventListBtn = onView(ViewMatchers.withId(R.id.action_list));
        addEventBtn = onView(withId(R.id.fab_button_add_event));
        listEventRV = new RecyclerViewHelper(R.id.list_events);
        waitingForLocationLayout = onView(withId(R.id.layout_waiting_for_location));
        map = new ExploreMapHelper();
    }

    public ExploreHelper openList() {
        actionEventListBtn
            .perform(click());
        return this;
    }

    public ExploreHelper addEvent() {
        addEventBtn.perform(click());
        return this;
    }

    public void openDrawer() {
        actionDrawer
                .perform(click());
    }

    public RecyclerViewHelper getListEvent() {
        return listEventRV;
    }

    public ExploreMapHelper getMap(){
        return map;
    }

    public ExploreHelper skipRequestLocation() {
        onView(withId(R.id.action_skip))
                .perform(click());
        return this;
    }
}
