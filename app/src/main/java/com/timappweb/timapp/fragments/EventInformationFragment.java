package com.timappweb.timapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.timappweb.timapp.data.entities.UserEventStatusEnum;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.databinding.FragmentEventInformationBinding;
import com.timappweb.timapp.listeners.OnTabSelectedListener;
import com.timappweb.timapp.map.MapFactory;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.callbacks.PublishInEventCallback;
import com.timappweb.timapp.rest.callbacks.RequestFailureCallback;
import com.timappweb.timapp.rest.callbacks.UpdateEventCallback;
import com.timappweb.timapp.rest.managers.HttpCallManager;
import com.timappweb.timapp.utils.DelayedCallHelper;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.ConfirmDialog;
import com.timappweb.timapp.views.SimpleTimerView;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import retrofit2.Response;


public class
EventInformationFragment extends EventBaseFragment implements OnMapReadyCallback,
        OnTabSelectedListener, LocationManager.LocationListener,
        android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener {

    private static final long           DELAY_REMOTE_UPDATE_STATUS_MILLS    = 0;
    private float                       ZOOM_LEVEL_CENTER_MAP   = 12.0f;

    // -

    private static final String         TAG                     = "EventInformationFrag";
    private ObservableScrollView        mScrollView;

    private MapView                     mapView                 = null;
    private FragmentEventInformationBinding mBinding;
    private SimpleTimerView             tvCountPoints;

    private TextView                    statusTv;
    private View                        crossOverView;
    private View                        pointsLayout;
    private TextView                    overText;
    private View                        statusLayout;
    private FloatingActionButton        activatedStatusButton;
    private FloatingActionButton        disabledStatusButton;
    private View                        btnRequestNavigation;
    private SwipeRefreshLayout          swipeRefreshLayout;

    private boolean                     isStatusLoading = false;


    public EventInformationFragment() {
        setTitle(R.string.title_fragment_info);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding  = DataBindingUtil.inflate(inflater, R.layout.fragment_event_information, container, false);
        View view = mBinding.getRoot();

        initVariables(view);
        setListeners();

        MaterialViewPagerHelper.registerScrollView(getActivity(), mScrollView, null);
        swipeRefreshLayout.setOnRefreshListener(this);

        mapView.onCreate(null);
        mapView.getMapAsync(this);

        updateEventBinding();
        LocationManager.addOnLocationChangedListener(this);
        updateStatusButtonActivation();

        return view;
    }

    private void initVariables(View view) {
        mScrollView = (ObservableScrollView) view.findViewById(R.id.scrollView);
        mapView = (MapView) view.findViewById(R.id.map);

        statusTv = (TextView) view.findViewById(R.id.status_text);
        activatedStatusButton = (FloatingActionButton) view.findViewById(R.id.status_button_activated);
        disabledStatusButton = (FloatingActionButton) view.findViewById(R.id.status_button_disabled);
        tvCountPoints = (SimpleTimerView) view.findViewById(R.id.points_text);

        crossOverView = view.findViewById(R.id.cross_overview);
        pointsLayout = view.findViewById(R.id.points_layout);
        overText = (TextView) view.findViewById(R.id.over_text);
        statusLayout = view.findViewById(R.id.status_layout);
        btnRequestNavigation = view.findViewById(R.id.button_nav);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
    }

    private void setListeners() {
        btnRequestNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchNavigation();
            }
        });
        activatedStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatus(false);
            }
        });
        disabledStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeStatus(true);
            }
        });
    }

    private void launchNavigation() {
        Event event = getEvent();
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=" + event.latitude + "," + event.longitude)); // TODO constant
        startActivity(intent);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCountPoints.initTimer(getEvent().getPoints());

        updateOverView();
    }

    @Override
    public void onDestroyView() {
        LocationManager.removeLocationListener(this);
        super.onDestroyView();
    }

    private void updateOverView() {
        if(eventActivity.getEvent() != null && eventActivity.getEvent().isOver()) {
            crossOverView.setVisibility(View.VISIBLE);
            pointsLayout.setVisibility(View.GONE);
            overText.setVisibility(View.VISIBLE);
            statusLayout.setVisibility(View.GONE);
        }
        else {
            crossOverView.setVisibility(View.GONE);
            pointsLayout.setVisibility(View.VISIBLE);
            overText.setVisibility(View.GONE);
            statusLayout.setVisibility(View.VISIBLE);
        }
    }

    public void turnComingOn() {
        isStatusLoading = true;
        changeStatus(true);
    }

    private void changeStatus(final boolean activated) {
        int visibilityAsked = activated ? View.VISIBLE : View.GONE;
        if(activatedStatusButton.getVisibility() == visibilityAsked) {
            return;
        }

        final Event event = getEvent();

        final Context context = MyApplication.getApplicationBaseContext();
        final UserEventStatusEnum newStatus = activated
                ? (event.isUserAround() ? UserEventStatusEnum.HERE : UserEventStatusEnum.COMING)
                :  UserEventStatusEnum.GONE;

        HttpCallManager manager = EventStatusManager.instance().add(context, event, newStatus, DELAY_REMOTE_UPDATE_STATUS_MILLS);
        if (manager != null){
            final Animation scaleDown = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_down);
            scaleDown.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(activated) {
                        disabledStatusButton.setVisibility(View.GONE);
                    }
                    else {
                        activatedStatusButton.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            if(activated) {
                disabledStatusButton.startAnimation(scaleDown);
            }
            else {
                activatedStatusButton.startAnimation(scaleDown);
            }
            manager
                    .onResponse(new PublishInEventCallback(event, MyApplication.getCurrentUser()))
                    .onResponse(new HttpCallback<UserEvent>() {
                        @Override
                        public void successful(UserEvent userEvent) {
                            showActivatedButton(activated, true);

                            if (userEvent == null){
                                // TODO update button to default
                                return;
                            }

                            if (userEvent.status != newStatus){
                                Log.w(TAG, "User status is not the on expected. Expected: " + newStatus + ". Actual: " + userEvent.status);
                                // TODO Jack && Steph set the right button according to userEvent.status
                            }

                            if(userEvent.status == UserEventStatusEnum.COMING) {
                                //Delay to see the animation before the dialog
                                DelayedCallHelper.create(R.integer.duration_scale, new DelayedCallHelper.Callback() {
                                    @Override
                                    public void onTime() {
                                        //Dialog
                                        ConfirmDialog.yesNoMessage(getActivity(),
                                                getString(R.string.launch_navigation_message),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if(which == DialogInterface.BUTTON_POSITIVE) {
                                                            launchNavigation();
                                                        }
                                                    }
                                                }
                                        )
                                                .create()
                                                .show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void notSuccessful() {
                            Toast.makeText(eventActivity, R.string.action_performed_not_successful, Toast.LENGTH_SHORT).show();
                            showActivatedButton(!activated, true);
                        }

                    })
                    .onError(new RequestFailureCallback(){
                        @Override
                        public void onError(Throwable error) {
                            Toast.makeText(eventActivity, R.string.no_network_access, Toast.LENGTH_SHORT).show();
                            showActivatedButton(!activated, true);
                        }
                    })
                    .onFinally(new HttpCallManager.FinallyCallback() {
                        @Override
                        public void onFinally(Response response, Throwable error) {
                            isStatusLoading = false;
                        }
                    });
        }
    }

    private void showActivatedButton(final boolean activated, boolean animate) {
        if(animate) {
            final Animation scaleUp = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_up);
            scaleUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(activated) {
                        activatedStatusButton.setVisibility(View.VISIBLE);
                        statusTv.setAlpha(1f);
                    }
                    else {
                        disabledStatusButton.setVisibility(View.VISIBLE);
                        statusTv.setAlpha(0.1f);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            if(activated) {
                activatedStatusButton.startAnimation(scaleUp);
            }
            else {
                disabledStatusButton.startAnimation(scaleUp);
            }
        }
        else {
            statusTv.setAlpha(activated ? 1f : 0.1f);
        }
        activatedStatusButton.setVisibility(activated ? View.VISIBLE : View.GONE);
        disabledStatusButton.setVisibility(activated ? View.GONE : View.VISIBLE);
    }

    public void updateStatusButtonActivation(){
        //This method checks if the button should be enabled and updates it
        Event event = getEvent();
        if(event == null) {
            Log.d(TAG, "Event is null");
            return;
        }
        showActivatedButton(EventStatusManager.hasUserStatus(event, UserEventStatusEnum.COMING)
                || EventStatusManager.hasUserStatus(event, UserEventStatusEnum.HERE), false);

        mBinding.notifyChange();
    }

    public void updateEventBinding(){
        mBinding.setEvent(getEvent());
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is now ready!");
        MapFactory.initMap(googleMap, false);
        Event event = eventActivity.getEvent();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(event.getPosition(), ZOOM_LEVEL_CENTER_MAP));
        googleMap.addMarker(event.getMarkerOption());
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
        tvCountPoints.animPointsTo(newPoints);
    }

    @Override
    public void onTabSelected() {
        if(mScrollView!=null) {
            mScrollView.smoothScrollTo(0,0);
        }
    }

    @Override
    public void onTabUnselected() {

    }

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        //TODO Steph : remove isStatusLoading variable and find another way
        if(!isStatusLoading) {
            updateStatusButtonActivation();
        }
    }

    @Override
    public void onRefresh() {
        RestClient.buildCall(RestClient.service()
                .updateEventInfo(getEvent().getRemoteId(), (getEvent().hasPicture() ? getEvent().picture.getRemoteId() : 0)))
                    .onResponse(new UpdateEventCallback(getEvent()))
                    .onFinally(new HttpCallManager.FinallyCallback() {
                        @Override
                        public void onFinally(Response response, Throwable error) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    })
                    .perform();
    }
}
