package com.timappweb.timapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.DrawerActivity;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.listeners.OnExploreTabSelectedListener;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;

import java.util.List;

public class ExplorePlacesFragment extends Fragment implements OnExploreTabSelectedListener {

    private static final String TAG = "ExplorePlaceFragment";
    private PlacesAdapter placesAdapter;
    private ExploreFragment exploreFragment;
    private DrawerActivity drawerActivity;
    private View newEventButton;
    private View progressView;
    private View noEventsView;
    private RecyclerView rvPlaces;
    //private EachSecondTimerTask eachSecondTimerTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View root = inflater.inflate(R.layout.fragment_explore_places, container, false);

        exploreFragment = (ExploreFragment) getParentFragment();
        drawerActivity = (DrawerActivity) exploreFragment.getActivity();

        //Views
        rvPlaces = (RecyclerView) root.findViewById(R.id.list_places);
        progressView = root.findViewById(R.id.loading_view);
        noEventsView = root.findViewById(R.id.no_events_view);
        newEventButton = root.findViewById(R.id.fab);
        newEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.addPostStepLocate(getContext());
            }
        });

        return root;
    }

    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");

        /*eachSecondTimerTask = EachSecondTimerTask.add(new TimeTaskCallback() {
            @Override
            public void update() {
                placesAdapter.notifyDataSetChanged();
            }
        })
*/
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        //eachSecondTimerTask.cancel();
    }

    @Override
    public void onTabSelected() {
        Log.d(TAG, "ExplorePlacesFragment is now selected");
        if(placesAdapter==null) {
            Log.d(TAG, "Initializing PlacesAdapter");
            initPlaceAdapter();
        }
        Log.d(TAG, "Loading "+placesAdapter.getData().size()+" places in List");
        placesAdapter.clear();
        ExploreMapFragment exploreMapFragment = exploreFragment.getExploreMapFragment();
        List<Place> markers = exploreFragment.getAreaRequestHistory().getInsideBoundsItems(exploreMapFragment.getMapBounds());
        placesAdapter.setData(markers);
        if(placesAdapter.getData().size()==0) {
            noEventsView.setVisibility(View.VISIBLE);
        } else {
            noEventsView.setVisibility(View.GONE);
        }
    }

    private void initPlaceAdapter() {
        //RV
        rvPlaces.setLayoutManager(new LinearLayoutManager(getContext()));

        //Adapter
        placesAdapter = new PlacesAdapter(getContext());
        rvPlaces.setAdapter(placesAdapter);
        placesAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                IntentsUtils.viewSpecifiedPlace(getContext(), placesAdapter.getItem(position));
            }
        });
    }
}
