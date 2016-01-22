package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;

public class PlacePeopleFragment extends Fragment {

    private static final String TAG = "PlaceTagsFragment";
    TagsAndCountersAdapter mTagsAndCountersAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context= getActivity().getApplicationContext();

        View root = inflater.inflate(R.layout.fragment_place_people, container, false);

        //Create ListView
        //////////////////////////////////////////////////////////////////////////////
        //Find listview in XML
        ListView lvTags = (ListView) root.findViewById(R.id.list_people);

        // pass context and data to the custom adapter
        //mTagsAndCountersAdapter = new TagsAndCountersAdapter(context);
        //lvTags.setAdapter(mTagsAndCountersAdapter);

        return root;
    }
}
