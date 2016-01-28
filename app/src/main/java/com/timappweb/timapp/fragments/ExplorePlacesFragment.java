package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.PlacesAdapter;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.List;

public class ExplorePlacesFragment extends Fragment {

    private static final String TAG = "PlaceTagsFragment";
    private PlacesAdapter   placesAdapter;
    private ExploreFragment exploreFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        exploreFragment = (ExploreFragment) getParentFragment();

        View root = inflater.inflate(R.layout.fragment_explore_places, container, false);

        //Initialize variables
        ListView lvTags = (ListView) root.findViewById(R.id.list_places);

        placesAdapter = new PlacesAdapter(getContext());
        exploreFragment.getDataLoader().setPlacesAdapter(placesAdapter);
        lvTags.setAdapter(placesAdapter);

        placesAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                IntentsUtils.viewPlaceFromMap(getContext(), placesAdapter.getItem(position));
            }
        });

        return root;
    }

    public void onResume(){
        super.onResume();
    }

    private void loadData() {
        //TODO : load data
    }

    private void loadDummyData(){
        Place place = new Place(1,0,0,"Event Test 1");
        Place place2 = new Place(1,0,0,"Event Test 2");
        Place place3 = new Place(1,0,0,"Event Test 3");
        placesAdapter.add(place);
        placesAdapter.add(place2);
        placesAdapter.add(place3);
    }
}
