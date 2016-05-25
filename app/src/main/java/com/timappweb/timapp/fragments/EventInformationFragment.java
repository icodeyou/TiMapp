package com.timappweb.timapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.utils.DistanceHelper;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.EventButtonsView;
import com.timappweb.timapp.views.HorizontalTagsRecyclerView;
import com.timappweb.timapp.views.SimpleTimerView;


public class EventInformationFragment extends EventBaseFragment {

    private static final String TAG = "EventInformationFragment";
    private ObservableScrollView mScrollView;
    private SimpleTimerView tvCountPoints;
    private TextView tvCountComing;
    private TextView tvCountHere;
    private TextView distanceText;
    private View distanceLayout;
    private TextView descriptionTv;
    private ImageView smallCategoryIcon;
    private TextView tvTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_event_information, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mScrollView = (ObservableScrollView) view.findViewById(R.id.scrollView);
        tvCountPoints = (SimpleTimerView) view.findViewById(R.id.white_points_text);
        tvTime = (TextView) view.findViewById(R.id.time_place);
        //categoryIcon = (ImageView) findViewById(R.id.image_category_place);
        //smallCategoryIcon = (ImageView) findViewById(R.id.image_small_category_place);
        //gradientBottomView = findViewById(R.id.bottom_gradient_event);
        //gradientBottomViewIfPadding = findViewById(R.id.bottom_gradient_if_padding);
        //separator = findViewById(R.id.separator);
        //descriptionView = findViewById(R.id.description_event);
        descriptionTv = (TextView) view.findViewById(R.id.description_textview);
        //matchButton = (SelectableFloatingButton) findViewById(R.id.match_button);
        distanceLayout = view.findViewById(R.id.distance_layout);
        distanceText = (TextView) view.findViewById(R.id.distance_text);

        tvCountComing = (TextView) view.findViewById(R.id.count_coming_text);
        tvCountHere = (TextView) view.findViewById(R.id.count_here_text);

        MaterialViewPagerHelper.registerScrollView(getActivity(), mScrollView, null);

        updateView();
    }

    public void updateView(){
        Event event = eventActivity.getEvent();
        tvCountPoints.initTimer(event.getPoints() * 1000);
        tvCountComing.setText(event.count_coming == null ? "0" : event.count_coming.toString());
        tvCountHere.setText(event.count_here == null ? "0" : event.count_here.toString());
        tvTime.setText(event.getTime());

        if(event.hasDescription()) {
            descriptionTv.setText(event.description);
            descriptionTv.setVisibility(View.VISIBLE);
        } else {
            descriptionTv.setVisibility(View.GONE);
        }


        if (LocationManager.hasLastLocation()){
            distanceLayout.setVisibility(View.VISIBLE);
            event.updateDistanceFromUser();
            distanceText.setText(DistanceHelper.prettyPrint(event.getDistanceFromUser()));
        }
        else {
            distanceLayout.setVisibility(View.GONE);
        }

    }
}
