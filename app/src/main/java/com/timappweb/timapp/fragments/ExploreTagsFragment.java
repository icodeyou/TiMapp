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
import com.timappweb.timapp.adapters.ListTagAdapter;
import com.timappweb.timapp.entities.Tag;

import java.util.ArrayList;

public class ExploreTagsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context= getActivity().getApplicationContext();

        View root = inflater.inflate(R.layout.fragment_explore_tags, container, false);

        //Create ListView
        //////////////////////////////////////////////////////////////////////////////
        //Find listview in XML
        ListView lvTags = (ListView) root.findViewById(R.id.list_tags_explore);

        // pass context and data to the custom adapter
        ListTagAdapter listTagAdapter = new ListTagAdapter(context,generateData());

        //Set adapter
        lvTags.setAdapter(listTagAdapter);


        return root;
    }

    private ArrayList<Tag> generateData() {
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(new Tag("#friteschezjojo", 1587));
        tags.add(new Tag("#boeing", 747));
        tags.add(new Tag("#airbus", 380));
        tags.add(new Tag("#lolilol", 185));
        tags.add(new Tag("#whatever", 184));
        tags.add(new Tag("#salt", 154));
        tags.add(new Tag("#beer", 146));
        tags.add(new Tag("#idontknowwhattosay", 130));
        tags.add(new Tag("#nowords", 114));
        tags.add(new Tag("#amazing", 104));
        tags.add(new Tag("#wtf", 85));
        tags.add(new Tag("#youhavetoseeittobelieveit", 55));
        tags.add(new Tag("#ohmygod", 30));
        tags.add(new Tag("#thisissofunny", 21));
        tags.add(new Tag("#beach", 14));
        return tags;
    }
}
