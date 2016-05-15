package com.timappweb.timapp.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.PlaceStatusManager;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.PlaceStatus;
import com.timappweb.timapp.listeners.BinaryActionListener;
import com.timappweb.timapp.listeners.SelectableButtonListener;

/**
 * Created by stephane on 5/15/2016.
 */
public class EventButtonsView extends RelativeLayout {

    private static final int TIMELAPSE_BUTTONS_APPEAR_ANIM      = 800;
    private static final int TIMELAPSE_BUTTONS_DISAPPEAR_ANIM   = 300;

    // =============================================================================================

    private SelectableFloatingButton matchButton;
    private View postButtons;
    private View picButton;
    private View tagButton;
    private View peopleButton;
    private SelectableFloatingButton comingButton;

    private AlphaAnimation postButtonsAppear;
    private AlphaAnimation postButtonsDisappear;
    private View gpsLocation;
    private Place event;
    private View viewGroupHere;
    private View viewGroupComing;

    public EventButtonsView(Context context) {
        super(context);
        this.init();
    }

    public EventButtonsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public EventButtonsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }


    // =============================================================================================

    public void setEvent(Place event){
        this.event = event;
    }

    // =============================================================================================


    private void init() {
        inflate(getContext(), R.layout.event_buttons, this);

        matchButton = (SelectableFloatingButton) findViewById(R.id.match_button);
        comingButton = (SelectableFloatingButton) findViewById(R.id.coming_button);
        postButtons = findViewById(R.id.post_buttons);
        picButton = findViewById(R.id.post_pic);
        tagButton = findViewById(R.id.post_tags);
        peopleButton = findViewById(R.id.post_people);
        gpsLocation = findViewById(R.id.waiting_for_gps_location);
        viewGroupHere = findViewById(R.id.event_buttons_around);
        viewGroupComing = findViewById(R.id.event_buttons_away);

        //Buttons appearance
        postButtonsAppear = new AlphaAnimation(0, 1);
        postButtonsAppear.setDuration(TIMELAPSE_BUTTONS_APPEAR_ANIM);
        //Buttons Disappearance
        postButtonsDisappear = new AlphaAnimation(1, 0);
        postButtonsDisappear.setDuration(TIMELAPSE_BUTTONS_DISAPPEAR_ANIM);


        comingButton.setSelectableListener(new SelectableButtonListener() {
            @Override
            public void updateUI(boolean enabled) {
                if (enabled) {
                    comingButton.setImageResource(R.drawable.ic_coming_guy_white);
                    comingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark)));
                } else {
                    comingButton.setImageResource(R.drawable.ic_coming_guy);
                    comingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
                }
                postButtons.setVisibility(View.GONE);
            }

            @Override
            public boolean performEnabled() {
                addPlaceStatus(UserPlaceStatusEnum.COMING);
                return true;
            }

            @Override
            public boolean performDisabled() {
                removePlaceStatus();
                return false;
            }

        });

        matchButton.setSelectableListener(new SelectableButtonListener() {

            @Override
            public void updateUI(boolean enabled) {
                if (enabled) {
                    matchButton.setImageResource(R.drawable.match_white);
                    matchButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark)));
                } else {
                    matchButton.setImageResource(R.drawable.match_red);
                    matchButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
                }
                //if (placeStatus != null && placeStatus.status == UserPlaceStatusEnum.COMING){
            }

            @Override
            public boolean performEnabled() {
                addPlaceStatus(UserPlaceStatusEnum.HERE);
                //fragmentTags.getEventView().updatePointsView(true);
                postButtons.startAnimation(postButtonsAppear);
                return true;
            }

            @Override
            public boolean performDisabled() {
                removePlaceStatus();
                //fragmentTags.getEventView().updatePointsView(false);
                postButtons.startAnimation(postButtonsDisappear);
                return true;
            }
        });



        picButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openAddPictureActivity();
                //pager.setCurrentItem(0);
            }
        });
        tagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openAddTagsActivity();
            }
        });
        peopleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openAddPeopleActivity();
            }
        });

    }

    public void updateUI() {
        if (!MyApplication.isLoggedIn()) {
            viewGroupHere.setVisibility(View.GONE);
            viewGroupComing.setVisibility(View.GONE);
            gpsLocation.setVisibility(View.GONE);
            return;
        }

        PlaceStatus placeStatus = PlaceStatusManager.getStatus(event);

        if (placeStatus != null){
            viewGroupComing.setVisibility(placeStatus.status == UserPlaceStatusEnum.COMING ? VISIBLE : GONE);
            viewGroupHere.setVisibility(placeStatus.status == UserPlaceStatusEnum.HERE ? VISIBLE : GONE);
        }
        else if (!MyApplication.hasLastLocation()){
            viewGroupHere.setVisibility(View.GONE);
            viewGroupComing.setVisibility(View.GONE);
            gpsLocation.setVisibility(View.VISIBLE);
        }
        else if (event.isUserAround()){
            viewGroupHere.setVisibility(View.VISIBLE);
            viewGroupComing.setVisibility(View.GONE);
            gpsLocation.setVisibility(View.GONE);
        }
        else{
            viewGroupHere.setVisibility(View.GONE);
            viewGroupComing.setVisibility(View.VISIBLE);
            gpsLocation.setVisibility(View.GONE);
        }

    }



    private void addPlaceStatus(final UserPlaceStatusEnum status) {
        if (event == null){
            return;
        }
        // TODO fine location
        // if (!MyApplication.hasFineLocation()) {
        //     Toast.makeText(this, R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
        //     return;
        // }
        PlaceStatusManager.instance().add(getContext(), event, status, new BinaryActionListener() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure() {
                //eventButtons.setComingState(false);
            }

            @Override
            public void onFinish() {
                //eventButtons.setComingState(true);
            }

        });
    }
    private void removePlaceStatus() {
        if (event == null){
            return;
        }
        PlaceStatusManager.instance().cancel(getContext(), event, new BinaryActionListener() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure() {
                //matchButton.setStateOn(true);
            }

            @Override
            public void onFinish() {
                //matchButton.setEnabled(true);
            }
        });
    }

}
