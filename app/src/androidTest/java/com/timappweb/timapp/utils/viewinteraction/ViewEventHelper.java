package com.timappweb.timapp.utils.viewinteraction;

import android.support.test.espresso.ViewInteraction;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.utils.ActivityHelper;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Stephane on 08/09/2016.
 */
public class ViewEventHelper {

    private final ViewInteraction viewPager;
    private final RecyclerViewHelper pictureRV;
    private final EventActionButtons actionBtns;
    private int currentPosition = EventActivity.INITIAL_FRAGMENT_PAGE;

    public ViewEventHelper() {
        viewPager = onView(withId(R.id.event_viewpager));
        pictureRV = new RecyclerViewHelper(R.id.pictures_rv);
        actionBtns = new EventActionButtons();
    }

    public ViewEventHelper swipeToPictureTab(){
        swipeTo(EventActivity.PAGER_PICTURE);
        return this;
    }
    public ViewEventHelper swipeToTagsTab(){
        swipeTo(EventActivity.PAGER_TAG);
        return this;
    }
    public ViewEventHelper swipeToInfoTab(){
        swipeTo(EventActivity.PAGER_INFO);
        return this;
    }
    public ViewEventHelper swipeToPeopleTab(){
        swipeTo(EventActivity.PAGER_PEOPLE);
        return this;
    }


    public ViewEventHelper addPicture(){
        actionBtns.camera();
        return this;
    }
    public ViewEventHelper addTags(){
        actionBtns
                .tags();
        return this;
    }
    public ViewEventHelper invitePeople(){
        actionBtns.invite();
        return this;
    }

    public ViewEventHelper viewPicture(int position){
        swipeToPictureTab();
        pictureRV.clickItem(position);
        return this;
    }

    public void startNavigation() {
        swipeToInfoTab();
        onView(withId(R.id.button_nav)).perform(scrollTo());
        ActivityHelper.btnClick(R.id.button_nav);
    }

    // ---------------------------------------------------------------------------------------------

    private void swipeTo(final int position){
        while (currentPosition != position){
            if (currentPosition > position ){
                viewPager.perform(swipeLeft());
                currentPosition--;
            }
            else{
                viewPager.perform(swipeRight());
                currentPosition++;
            }
        }
    }

}
