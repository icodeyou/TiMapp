package com.timappweb.timapp.fragments;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventCategory;
import com.timappweb.timapp.databinding.FragmentEventInformationBinding;
import com.timappweb.timapp.exceptions.UnknownCategoryException;
import com.timappweb.timapp.utils.DistanceHelper;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.utils.location.MyLocationProvider;
import com.timappweb.timapp.views.SimpleTimerView;
import com.timappweb.timapp.views.controller.EventStateButtonController;

import java.util.HashMap;


public class EventInformationFragment extends EventBaseFragment {

    private float ZOOM_LEVEL_CENTER_MAP = 12.0f;

    private static final String TAG = "EventInformationFrag";
    private ObservableScrollView mScrollView;
    private TextView distanceText;
    private ImageView smallCategoryIcon;

    private MapView mapView = null;
    private GoogleMap gMap;
    private ImageView eventCategoryIcon;
    private FragmentEventInformationBinding mBinding;
    private FloatingActionButton mHereButton;
    private FloatingActionButton mComingButton;
    private Button btnRequestNavigation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //return inflater.inflate(R.layout.fragment_event_information, container, false);
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

        Event event = eventActivity.getEvent();
        mHereButton = (FloatingActionButton) view.findViewById(R.id.here_button);
        new EventStateButtonController(getContext(), mHereButton, event, UserPlaceStatusEnum.HERE).initState();
        mComingButton = (FloatingActionButton) view.findViewById(R.id.coming_button);
        new EventStateButtonController(getContext(), mComingButton, event, UserPlaceStatusEnum.COMING).initState();

        btnRequestNavigation = (Button) view.findViewById(R.id.request_gps_path);
        btnRequestNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Event event = eventActivity.getEvent();
                Location location = LocationManager.getLastLocation();
                String destinationAddr = "";
                //if (location != null){
                //    destinationAddr = "&saddr=" + location.getLatitude() + "," + location.getLongitude();
                //}
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + event.latitude + "," + event.longitude + destinationAddr));
                startActivity(intent);
            }
        });


        MaterialViewPagerHelper.registerScrollView(getActivity(), mScrollView, null);

        initMap();
        updateView();
    }

    public void updateView(){
        Event event = eventActivity.getEvent();
        mBinding.setEvent(event);

        EventCategory category = event.getCategoryWithDefault();
        eventCategoryIcon.setImageResource(category.getIconBlackResId());
    }


    private void initMap(){
        mapView.onCreate(null);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "Map is now ready!");
                gMap = googleMap;
            }
        });
        loadMapIfNeeded();
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
        this.loadMapIfNeeded();
    }

    private void loadMapIfNeeded() {
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
    }

}
