package com.timappweb.timapp.fragments;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.EventStatusManager;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.data.models.EventStatus;
import com.timappweb.timapp.databinding.FragmentEventInformationBinding;
import com.timappweb.timapp.map.MapFactory;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.SimpleTimerView;


public class EventInformationFragment extends EventBaseFragment implements OnMapReadyCallback {

    private static final int            TIMELAPSE_HOT_ANIM      = 2000;
    private static final long           DELAY_REMOTE_UPDATE_STATUS_MILLS    = 2 * 1000;
    private float                       ZOOM_LEVEL_CENTER_MAP   = 12.0f;

    private static final String         TAG                     = "EventInformationFrag";
    private ObservableScrollView        mScrollView;
    private TextView                    distanceText;
    private ImageView                   smallCategoryIcon;

    private MapView                     mapView                 = null;
    private GoogleMap                   gMap;
    private ImageView                   eventCategoryIcon;
    private FragmentEventInformationBinding mBinding;
    private View                        btnRequestNavigation;
    private SimpleTimerView             tvCountPoints;

    private ValueAnimator               animator;
    private boolean                     hotPoints               = false;

    private SwitchCompat                switchButton;
    private View                        rateButtons;
    private View                        flameView;
    private View                        mainLayout;
    private TextView                    statusTv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding  = DataBindingUtil.inflate(inflater, R.layout.fragment_event_information, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mScrollView = (ObservableScrollView) view.findViewById(R.id.scrollView);
        mapView = (MapView) view.findViewById(R.id.map);

        //distanceLayout = view.findViewById(R.id.distance_layout);
        distanceText = (TextView) view.findViewById(R.id.distance_text);
        eventCategoryIcon = (ImageView) view.findViewById(R.id.image_category_place);

        final Event event = eventActivity.getEvent();


        mainLayout = view.findViewById(R.id.main_layout);
        statusTv = (TextView) view.findViewById(R.id.status_text);
        //rateButtons = view.findViewById(R.id.here_view);
        flameView = view.findViewById(R.id.points_icon);
        switchButton = (SwitchCompat) view.findViewById(R.id.switch_button);

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                updatePointsView(isChecked);
                int colorStatusText = isChecked ? R.color.colorPrimary : R.color.DarkGray;
                statusTv.setTextColor(ContextCompat.getColor(getContext(),colorStatusText));

                UserPlaceStatusEnum newStatus = isChecked
                        ? (event.isUserAround() ? UserPlaceStatusEnum.HERE : UserPlaceStatusEnum.COMING)
                        :  UserPlaceStatusEnum.GONE;

                HttpCallManager manager = EventStatusManager.instance().add(getContext(), event, newStatus, DELAY_REMOTE_UPDATE_STATUS_MILLS);
                if (manager != null){
                    manager.onResponse(new HttpCallback() {
                            @Override
                            public void notSuccessful() {
                                // TODO revert status changed
                            }

                        })
                        .onError(new RequestFailureCallback(){
                            @Override
                            public void onError(Throwable error) {
                                // TODO cancel
                                switchButton.setChecked(!isChecked);
                            }
                        });
                }
            }
        });

        tvCountPoints = (SimpleTimerView) view.findViewById(R.id.white_points_text);
        int initialTime = event.getPoints();
        tvCountPoints.initTimer(initialTime);

        btnRequestNavigation = view.findViewById(R.id.button_nav);
        btnRequestNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Event event = eventActivity.getEvent();
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + event.latitude + "," + event.longitude));
                startActivity(intent);
            }
        });

        MaterialViewPagerHelper.registerScrollView(getActivity(), mScrollView, null);

        mapView.onCreate(null);
        mapView.getMapAsync(this);

        updateView();

        updateUserStatusButton();
        LocationManager.addOnLocationChangedListener(new LocationManager.LocationListener() {
            @Override
            public void onLocationChanged(Location newLocation, Location lastLocation) {
                updateUserStatusButton();
                mBinding.notifyChange();
            }
        });
    }

    public void updateUserStatusButton(){
        Event event = eventActivity.getEvent();
        EventStatus statusInfo = EventStatusManager.getStatus(event);
        if (event.isUserAround()){
            statusTv.setText(getContext().getResources().getString(R.string.i_am_here));
            if (statusInfo != null ){
                switchButton.setChecked(statusInfo.status == UserPlaceStatusEnum.HERE);
            }
        }
        else{
            statusTv.setText(getContext().getResources().getString(R.string.i_am_coming));
            switchButton.setChecked(statusInfo != null && statusInfo.status == UserPlaceStatusEnum.COMING);
        }
    }

    public void updateView(){
        Event event = eventActivity.getEvent();
        mBinding.setEvent(event);

        EventCategory category = event.getCategoryWithDefault();
        eventCategoryIcon.setImageResource(category.getIconBlackResId());
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is now ready!");
        gMap = googleMap;
        MapFactory.initMap(gMap);
        Event event = eventActivity.getEvent();
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(event.getPosition(), ZOOM_LEVEL_CENTER_MAP));
        gMap.addMarker(event.getMarkerOption());
        eventActivity.initMapUI(mapView.getMap(), false);
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
        Log.d(TAG, "ExploreMapFragment.onResume()");
        //this.loadMapIfNeeded();
    }

    /*private void loadMapIfNeeded() {
        try {
            if (gMap == null){
                gMap = mapView.getMap();
            }
            gMap.setIndoorEnabled(true);
            Event event = eventActivity.getEvent();
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(event.getPosition(), ZOOM_LEVEL_CENTER_MAP));
            gMap.addMarker(event.getMarkerOption());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public void updatePointsView(boolean increase) {
        /*if(increase && !hotPoints) {
            hotPoints = true;
            icPoints.setImageResource(R.drawable.ic_hot);
            tvCountPoints.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        } else if(!increase && hotPoints){
            hotPoints = false;
            icPoints.setImageResource(R.drawable.ic_hot_white);
            tvCountPoints.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        } else {
            return;
        }*/

        animator = new ValueAnimator();
        tvCountPoints.cancelTimer();
        int initialPoints = tvCountPoints.getPoints();
        final int finalPoints;
        if(increase) {
            finalPoints = initialPoints+300;
        } else {
            if(initialPoints>300) {
                finalPoints = initialPoints-300;
            } else {
                finalPoints = 0;
            }
        }

        Log.d(TAG, "Initial points : " + initialPoints);
        Log.d(TAG, "Final points : " + finalPoints);
        animator.setObjectValues(initialPoints, finalPoints);
        animator.setDuration(TIMELAPSE_HOT_ANIM);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                tvCountPoints.setText(String.valueOf(animation.getAnimatedValue()));
            }
        });
        animator.start();
        if(finalPoints==0) {
            Log.d(TAG, "Set timer text to Over");
            tvCountPoints.setText(getString(R.string.counter_over));
        } else {
            Log.d(TAG, "Initializing timer to +" + finalPoints);
            tvCountPoints.initTimer(finalPoints*1000 + TIMELAPSE_HOT_ANIM);
        }
    }

}
