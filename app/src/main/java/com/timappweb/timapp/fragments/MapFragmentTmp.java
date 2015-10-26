package com.timappweb.timapp.fragments;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.timappweb.timapp.R;

public class MapFragmentTmp extends com.google.android.gms.maps.MapFragment {
    //an interval of update of a rule of a pop-up window.
    //for smoothness it is necessary 60 fps, that is 1000 ms / 60 = 16 ms between updates.
    private static final int POPUP_POSITION_REFRESH_INTERVAL = 16;

    //Handler, launching update of a window with the given interval
    private Handler handler;
    //Runnable which updates window position
    private Runnable positionUpdaterRunnable;
    //duration of animation of relocation of the camera
    private static final int ANIMATION_DURATION = 500;

    //coordinates of a point on the map traced at scrolling
    private LatLng trackedPosition;

    //offsets of the pop-up window, allowing to correct its rule concerning a label
    private int popupXOffset;
    private int popupYOffset;
    //label altitude
    private int markerHeight;

    //the listener who will update offsets at a window dimensional change
    private ViewTreeObserver. OnGlobalLayoutListener infoWindowLayoutListener;

    //the container of a pop-up window
    private View infoWindowContainer;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate (R.layout.fragment_maps_popup, null);

        FrameLayout containerMap = (FrameLayout) rootView.findViewById (R.id.container_map);
        View mapView = super.onCreateView (inflater, container, savedInstanceState);
        containerMap.addView (mapView, new FrameLayout.LayoutParams (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        infoWindowContainer = rootView.findViewById (R.id.container_popup);
        //we subscribe for a dimensional change of a pop-up window
        infoWindowLayoutListener = new InfoWindowLayoutListener ();
        infoWindowContainer.getViewTreeObserver ().addOnGlobalLayoutListener(infoWindowLayoutListener);

        return rootView;
    }

    //@Override
    public void onMapClick (LatLng latLng) {
        //cliques outside of a pop-up window and not on one of labels, we hide a pop-up window
        infoWindowContainer.setVisibility (View.INVISIBLE);
    }

    //@Override
    public boolean onMarkerClick (Marker marker) {
        //cliques on a label on a card
        //it is installed a traced point
        GoogleMap map = getMap ();
        Projection projection = map.getProjection ();
        trackedPosition = marker.getPosition ();
        //we move the camera
        Point trackedPoint = projection.toScreenLocation (trackedPosition);
        trackedPoint.y -= popupYOffset / 2;
        LatLng newCameraLocation = projection.fromScreenLocation (trackedPoint);
        map.animateCamera (CameraUpdateFactory.newLatLng (newCameraLocation), ANIMATION_DURATION, null);

        //it is filled and we show a window
        infoWindowContainer.setVisibility (View.VISIBLE);

        return true;
    }

    private class InfoWindowLayoutListener implements ViewTreeObserver. OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout () {
            //window sizes changed, we update offsets
            popupXOffset = infoWindowContainer.getWidth () / 2;
            popupYOffset = infoWindowContainer.getHeight ();
        }
    }
    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        super.onViewCreated (view, savedInstanceState);

        handler = new Handler (Looper.getMainLooper());
        positionUpdaterRunnable = new PositionUpdaterRunnable ();

        //it is launched periodic update
        handler.post (positionUpdaterRunnable);
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView ();

        //cleaning
        handler.removeCallbacks (positionUpdaterRunnable);
        handler = null;
    }

    private class PositionUpdaterRunnable implements Runnable {
        private int lastXPosition = Integer. MIN_VALUE;
        private int lastYPosition = Integer. MIN_VALUE;

        @Override
        public void run () {
            //the following turnover cycle is placed in queue
            handler.postDelayed (this, POPUP_POSITION_REFRESH_INTERVAL);

            //if the pop-up window is hidden, is done nothing
            if (trackedPosition != null && infoWindowContainer.getVisibility () == View.VISIBLE) {
                Point targetPosition = getMap ().getProjection ().toScreenLocation (trackedPosition);

                //if the window position did not change, is done nothing
                if (lastXPosition != targetPosition.x || lastYPosition != targetPosition.y) {
                    //we update a rule
                    AbsoluteLayout.LayoutParams overlayLayoutParams = (AbsoluteLayout.LayoutParams) infoWindowContainer.getLayoutParams ();
                    overlayLayoutParams.x = targetPosition.x - popupXOffset;
                    overlayLayoutParams.y = targetPosition.y - popupYOffset - markerHeight;
                    infoWindowContainer.setLayoutParams (overlayLayoutParams);

                    //we remember running coordinates
                    lastXPosition = targetPosition.x;
                    lastYPosition = targetPosition.y;
                }
            }
        }
    }
}
/**
 * Created by stephane on 8/30/2015.
 */
/*
public class MapFragmentTmp extends com.google.android.gms.maps. MapFragmentTmp {
    //duration of animation of relocation of the camera
    private static final int ANIMATION_DURATION = 500;

    //coordinates of a point on the map traced at scrolling
    private LatLng trackedPosition;

    //offsets of the pop-up window, allowing to correct its rule concerning a label
    private int popupXOffset;
    private int popupYOffset;
    //label altitude
    private int markerHeight;

    //the listener who will update offsets at a window dimensional change
    private ViewTreeObserver. OnGlobalLayoutListener infoWindowLayoutListener;

    //the container of a pop-up window
    private View infoWindowContainer;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate (R.layout.fragment, null);

        FrameLayout containerMap = (FrameLayout) rootView.findViewById (R.id.container_map);
        View mapView = super.onCreateView (inflater, container, savedInstanceState);
        containerMap.addView (mapView, new FrameLayout. LayoutParams (MATCH_PARENT, MATCH_PARENT));

        infoWindowContainer = rootView.findViewById (R.id.container_popup);
        //we subscribe for a dimensional change of a pop-up window
        infoWindowLayoutListener = new InfoWindowLayoutListener ();
        infoWindowContainer.getViewTreeObserver ().addOnGlobalLayoutListener (infoWindowLayoutListener);

        return rootView;
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView ();

        //we are removed behind themselves
        infoWindowContainer.getViewTreeObserver ().removeGlobalOnLayoutListener (infoWindowLayoutListener);
    }

    @Override
    public void onMapClick (LatLng latLng) {
        //cliques outside of a pop-up window and not on one of labels, we hide a pop-up window
        infoWindowContainer.setVisibility (INVISIBLE);
    }

    @Override
    public boolean onMarkerClick (Marker marker) {
        //cliques on a label on a card
        //it is installed a traced point
        GoogleMap map = getMap ();
        Projection projection = map.getProjection ();
        trackedPosition = marker.getPosition ();
        //we move the camera
        IntPoint trackedPoint = projection.toScreenLocation (trackedPosition);
        trackedPoint.y - = popupYOffset / 2;
        LatLng newCameraLocation = projection.fromScreenLocation (trackedPoint);
        map.animateCamera (CameraUpdateFactory.newLatLng (newCameraLocation), ANIMATION_DURATION, null);

        //it is filled and we show a window
        //┘
        infoWindowContainer.setVisibility (VISIBLE);

        return true;
    }

    private class InfoWindowLayoutListener implements ViewTreeObserver. OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout () {
            //window sizes changed, we update offsets
            popupXOffset = infoWindowContainer.getWidth () / 2;
            popupYOffset = infoWindowContainer.getHeight ();
        }
    }
}
*/