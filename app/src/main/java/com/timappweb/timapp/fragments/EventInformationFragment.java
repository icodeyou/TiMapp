package com.timappweb.timapp.fragments;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.config.EventStatusManager;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.entities.UserEventStatusEnum;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.databinding.FragmentEventInformationBinding;
import com.timappweb.timapp.listeners.OnTabSelectedListener;
import com.timappweb.timapp.map.MapFactory;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.PublishInEventCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.MySwitchCompat;
import com.timappweb.timapp.views.SimpleTimerView;

import retrofit2.Response;


public class EventInformationFragment extends EventBaseFragment implements OnMapReadyCallback, OnTabSelectedListener, LocationManager.LocationListener {

    private static final int            TIMELAPSE_HOT_ANIM      = 2000;
    private static final long           DELAY_REMOTE_UPDATE_STATUS_MILLS    = 2 * 1000;
    private float                       ZOOM_LEVEL_CENTER_MAP   = 12.0f;

    // -

    private static final String         TAG                     = "EventInformationFrag";
    private ObservableScrollView        mScrollView;
    private TextView                    distanceText;

    private MapView                     mapView                 = null;
    private GoogleMap                   gMap;
    private ImageView                   eventCategoryIcon;
    private FragmentEventInformationBinding mBinding;
    private SimpleTimerView             tvCountPoints;

    private ValueAnimator               animator;
    private boolean                     hotPoints               = false;

    private MySwitchCompat              switchButton;
    private View                        rateButtons;
    private View                        flameView;
    private View                        mainLayout;
    private TextView                    statusTv;
    private View                        statusImage;
    private View                        progressStatus;

    private boolean isStatusLoading = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding  = DataBindingUtil.inflate(inflater, R.layout.fragment_event_information, container, false);
        View view = mBinding.getRoot();

        initVariables(view);

        View btnRequestNavigation = view.findViewById(R.id.button_nav);
        btnRequestNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Event event = getEvent();
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + event.latitude + "," + event.longitude)); // TODO constant
                startActivity(intent);
            }
        });

        MaterialViewPagerHelper.registerScrollView(getActivity(), mScrollView, null);

        mapView.onCreate(null);
        mapView.getMapAsync(this);

        updateView();
        LocationManager.addOnLocationChangedListener(this);
        updateUserStatusButton();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCountPoints.initTimer(getEvent().getPoints());
    }

    private void initVariables(View view) {
        mScrollView = (ObservableScrollView) view.findViewById(R.id.scrollView);
        mapView = (MapView) view.findViewById(R.id.map);
        distanceText = (TextView) view.findViewById(R.id.distance_text);
        eventCategoryIcon = (ImageView) view.findViewById(R.id.image_category_place);

        mainLayout = view.findViewById(R.id.main_layout);
        statusTv = (TextView) view.findViewById(R.id.status_text);
        flameView = view.findViewById(R.id.points_icon);
        switchButton = (MySwitchCompat) view.findViewById(R.id.switch_button);
        statusImage = view.findViewById(R.id.ic_status);
        progressStatus = view.findViewById(R.id.status_progress);
        tvCountPoints = (SimpleTimerView) view.findViewById(R.id.points_text);
        switchButton.setOnCheckedChangeListener(new OnStatusChangedListener());
    }


    public void turnComingOn() {
        //TODO Steph: Find a better way than using a boolean
        isStatusLoading = true;
        switchButton.setChecked(true);
    }

    private void setStatusProgress(boolean isProgressViewEnabled) {
        if(isProgressViewEnabled) {
            statusImage.setVisibility(View.GONE);
            progressStatus.setVisibility(View.VISIBLE);
        } else {
            statusImage.setVisibility(View.VISIBLE);
            progressStatus.setVisibility(View.GONE);
        }
    }

    public void updateUserStatusButton(){
        Event event = getEvent();
        UserEvent statusInfo = EventStatusManager.getStatus(event);
        if (event.isUserAround()){
            switchButton.setChecked(statusInfo != null && statusInfo.status == UserEventStatusEnum.HERE);
        }
        else{
            switchButton.setChecked(statusInfo != null && statusInfo.status == UserEventStatusEnum.COMING);
        }

        mBinding.notifyChange();
    }

    public void updateView(){
        mBinding.setEvent(getEvent());
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
    }

    public void updatePointsView(int newPoints) {
        animator = new ValueAnimator();
        tvCountPoints.cancelTimer();
        int initialPoints = tvCountPoints.getPoints();
        final int pointsAdded = newPoints - tvCountPoints.getPoints();
        int finalPoints = initialPoints + pointsAdded - TIMELAPSE_HOT_ANIM/1000;

        Log.d(TAG, "Initial points : " + initialPoints + ". Final points : " + finalPoints);
        animator.setObjectValues(initialPoints, finalPoints);
        animator.setDuration(TIMELAPSE_HOT_ANIM);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                tvCountPoints.setText(String.valueOf(animation.getAnimatedValue()));
            }
        });
        animator.start();
        if(finalPoints==0) {
            tvCountPoints.cancelTimer();
            Log.d(TAG, "Set timer text to Over");
            tvCountPoints.setText(getString(R.string.counter_over));
        } else {
            Log.d(TAG, "Initializing timer to " + finalPoints);
            tvCountPoints.initTimer(finalPoints);
        }
    }

    @Override
    public void onTabSelected() {
        if(mScrollView!=null) {
            mScrollView.smoothScrollTo(0,0);
        }
    }

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        if(!isStatusLoading) {
            updateUserStatusButton();
        }
    }

    private class OnStatusChangedListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
            Event event = getEvent();
            UserEventStatusEnum newStatus = isChecked
                    ? (event.isUserAround() ? UserEventStatusEnum.HERE : UserEventStatusEnum.COMING)
                    :  UserEventStatusEnum.GONE;

            int colorStatusText = isChecked ? R.color.colorPrimary : R.color.DarkGray;
            statusTv.setTextColor(ContextCompat.getColor(getContext(),colorStatusText));
            if(progressStatus.getVisibility()==View.VISIBLE){
                setStatusProgress(false);
            }

            HttpCallManager manager = EventStatusManager.instance().add(getContext(), event, newStatus, DELAY_REMOTE_UPDATE_STATUS_MILLS);
            if (manager != null){
                setStatusProgress(true);

                manager
                    .onResponse(new PublishInEventCallback(event, MyApplication.getCurrentUser()))
                    .onResponse(new HttpCallback() {
                        @Override
                        public void successful(Object feedback) {

                        }

                        @Override
                        public void notSuccessful() {
                            Toast.makeText(eventActivity, R.string.action_performed_not_successful, Toast.LENGTH_SHORT).show();
                            switchButton.setCheckedNoTrigger(!isChecked);
                        }

                    })
                    .onError(new RequestFailureCallback(){
                        @Override
                        public void onError(Throwable error) {
                            Toast.makeText(eventActivity, R.string.no_network_access, Toast.LENGTH_SHORT).show();
                            switchButton.setCheckedNoTrigger(!isChecked);
                        }
                    })
                    .onFinally(new HttpCallManager.FinallyCallback() {
                        @Override
                        public void onFinally(Response response, Throwable error) {
                            setStatusProgress(false);
                            isStatusLoading = false;
                        }
                    });
            }
        }
    }
}
