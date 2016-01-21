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
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.List;

import retrofit.client.Response;

public class ExplorePlacesFragment extends Fragment {

    private static final String TAG = "PlaceTagsFragment";
    PlacesAdapter placesAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context= getActivity().getApplicationContext();

        View root = inflater.inflate(R.layout.fragment_explore_places, container, false);

        //Initialize variables
        ListView lvTags = (ListView) root.findViewById(R.id.list_places);

        placesAdapter = new PlacesAdapter(context);
        lvTags.setAdapter(placesAdapter);

        return root;
    }

    public void onResume(){
        super.onResume();
        //TODO : onResume
    }

    private void loadData() {
        //TODO : load data
    }
}
