package com.timappweb.timapp.views;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.location.Location;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.config.PlaceStatusManager;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventStatus;
import com.timappweb.timapp.listeners.BinaryActionListener;
import com.timappweb.timapp.listeners.SelectableButtonListener;
import com.timappweb.timapp.utils.location.LocationManager;

/**
 * Created by stephane on 5/15/2016.
 */
public class EventButtonsView extends RelativeLayout {

    private static final int TIMELAPSE_BUTTONS_APPEAR_ANIM      = 800;
    private static final int TIMELAPSE_BUTTONS_DISAPPEAR_ANIM   = 300;
    private static final String TAG = "EventButtonsView";

    // =============================================================================================

    private SelectableFloatingButton hereButton;
    private View postButtons;
    private View picButton;
    private View tagButton;
    private View peopleButton;
    private SelectableFloatingButton comingButton;

    private AlphaAnimation postButtonsAppear;
    private AlphaAnimation postButtonsDisappear;
    private View gpsLocation;
    private Event event;
    private View viewGroupHere;
    private View viewGroupComing;
    private View container;
    private View navigationButtons;

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

    public void setEvent(Event event){
        if (!event.hasLocalId()){
            Log.e(TAG, "Cannot set event because it's not saved a local instance: " + event);
            return;
        }
        this.event = event;


        updateUI();
    }

    // =============================================================================================


    private void init() {
        inflate(getContext(), R.layout.event_buttons, this);

        hereButton = (SelectableFloatingButton) findViewById(R.id.here_button);
        comingButton = (SelectableFloatingButton) findViewById(R.id.coming_button);
        container = findViewById(R.id.event_buttons_container);
        postButtons = findViewById(R.id.post_buttons);
        picButton = findViewById(R.id.post_pic);
        tagButton = findViewById(R.id.post_tags);
        peopleButton = findViewById(R.id.post_people);
        gpsLocation = findViewById(R.id.waiting_for_gps_location);
        viewGroupHere = findViewById(R.id.event_buttons_around);
        viewGroupComing = findViewById(R.id.event_buttons_away);
        navigationButtons = findViewById(R.id.request_gps_path);

        //Buttons appearance
        postButtonsAppear = new AlphaAnimation(0, 1);
        postButtonsAppear.setDuration(TIMELAPSE_BUTTONS_APPEAR_ANIM);
        postButtonsAppear.setFillAfter(true);
        //Buttons Disappearance
        postButtonsDisappear = new AlphaAnimation(1, 0);
        postButtonsDisappear.setDuration(TIMELAPSE_BUTTONS_DISAPPEAR_ANIM);
        postButtonsDisappear.setFillAfter(true);

        comingButton.setSelectableListener(new SelectableButtonListener() {


            @Override
            public void updateUI(boolean enabled) {
                if (enabled) {
                    comingButton.setImageResource(R.drawable.ic_coming_guy_white);
                    comingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark)));
                    navigationButtons.setVisibility(VISIBLE);
                } else {
                    comingButton.setImageResource(R.drawable.ic_coming_guy);
                    comingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
                    navigationButtons.setVisibility(GONE);
                }
            }

            @Override
            public boolean performEnabled() {
                addPlaceStatus(UserPlaceStatusEnum.COMING);
                return true;
            }

            @Override
            public boolean performDisabled() {
                removePlaceStatus(UserPlaceStatusEnum.COMING);
                return false;
            }

        });

        hereButton.setSelectableListener(new SelectableButtonListener() {

            @Override
            public void updateUI(boolean enabled) {
                if (enabled) {
                    hereButton.setImageResource(R.drawable.match_white);
                    hereButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark)));
                    postButtons.setVisibility(event.isUserAround() ? View.VISIBLE : GONE);
                } else {
                    hereButton.setImageResource(R.drawable.match_red);
                    hereButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.white)));
                    postButtons.setVisibility(View.GONE);
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
                removePlaceStatus(UserPlaceStatusEnum.HERE);
                //fragmentTags.getEventView().updatePointsView(false);
                postButtons.startAnimation(postButtonsDisappear);
                return true;
            }
        });

        picButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.postEvent(getContext(), event, IntentsUtils.ACTION_CAMERA);
                //openAddPictureActivity();
                //pager.setCurrentItem(0);
            }
        });
        tagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.postEvent(getContext(), event, IntentsUtils.ACTION_TAGS);
                //openAddTagsActivity();
            }
        });
        peopleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.postEvent(getContext(), event, IntentsUtils.ACTION_PEOPLE);
                //openAddPeopleActivity();
            }
        });

        navigationButtons.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String daddr = event.getPosition().latitude + "," + event.getPosition().longitude;
                //String saddr = LocationManager.getLastLocation().getLatitude() + "," + LocationManager.getLastLocation().getLongitude();
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + daddr));
                getContext().startActivity(intent);
            }
        });

        // TODO what happens when view is destroyed
        LocationManager.addOnLocationChangedListener(new LocationManager.LocationListener() {
            @Override
            public void onLocationChanged(Location newLocation, Location lastLocation) {
                EventButtonsView.this.updateUI();
            }
        });



        updateUI();
    }

    public void updateUI() {
        if (event == null){
            container.setVisibility(GONE);
            return;
        }
        container.setVisibility(VISIBLE);

        if (!MyApplication.isLoggedIn()) {
            viewGroupHere.setVisibility(View.GONE);
            viewGroupComing.setVisibility(View.GONE);
            gpsLocation.setVisibility(View.GONE);
            return;
        }

        EventStatus eventStatus = PlaceStatusManager.getStatus(event);

        if (eventStatus != null){
            switch (eventStatus.status){
                case HERE:
                case COMING:
                    updateUserStatus(eventStatus.status, true);
                    break;
                default:
                    viewGroupHere.setVisibility(GONE);
                    viewGroupHere.setVisibility(GONE);
                    gpsLocation.setVisibility(View.GONE);
            }
        }
        else if (!LocationManager.hasLastLocation()){
            viewGroupHere.setVisibility(View.GONE);
            viewGroupComing.setVisibility(View.GONE);
            gpsLocation.setVisibility(View.VISIBLE);
        }
        else if (event.isUserAround()){
            showGroupHere();
        }
        else{
            showGroupComing();
        }

    }

    private void showGroupComing(){
        viewGroupComing.setVisibility(VISIBLE);
        viewGroupHere.setVisibility(GONE);
        gpsLocation.setVisibility(GONE);
        comingButton.updateUI();
    }
    private void showGroupHere(){
        viewGroupHere.setVisibility(VISIBLE);
        viewGroupComing.setVisibility(GONE);
        gpsLocation.setVisibility(GONE);
        hereButton.updateUI();
    }

    private void addPlaceStatus(final UserPlaceStatusEnum status) {
        if (event == null){
            return;
        }
        PlaceStatusManager.instance().add(getContext(), event, status, new BinaryActionListener() {

            @Override
            public void onSuccess() {
                updateUserStatus(status, true);
            }

            @Override
            public void onFailure() {
                updateUserStatus(status, false);
            }

            @Override
            public void onFinish() {

            }

        });
    }
    private void removePlaceStatus(final UserPlaceStatusEnum status) {
        if (event == null){
            return;
        }
        PlaceStatusManager.instance().cancel(getContext(), event, new BinaryActionListener() {

            @Override
            public void onSuccess() {
                updateUserStatus(status, true);
            }

            @Override
            public void onFailure() {
                updateUserStatus(status, false);
            }

            @Override
            public void onFinish() {}
        });
    }

    public void updateUserStatus(UserPlaceStatusEnum status, boolean active) {
        switch (status){
            case HERE:
                this.hereButton.setStateOn(active);
                this.hereButton.setEnabled(true);
                showGroupHere();
                break;
            case COMING:
                this.comingButton.setStateOn(active);
                this.comingButton.setEnabled(true);
                showGroupComing();
                break;
        }
    }
}
