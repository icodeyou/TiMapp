package com.timappweb.timapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.views.parallaxviewpager.NotifyingScrollView;
import com.timappweb.timapp.views.parallaxviewpager.ScrollViewFragment;


public class EventInformationFragment extends ScrollViewFragment {

    private static final String TAG = "EventInformationFragment";

    private EventActivity eventActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_event_information, container, false);
        eventActivity = (EventActivity) getActivity();

        mScrollView = (NotifyingScrollView) root.findViewById(R.id.scrollview);
        setScrollViewOnScrollListener();

        return root;
    }

}
