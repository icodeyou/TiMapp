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
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Response;

public class PlaceTagsFragment extends Fragment {

    private static final String TAG = "PlaceTagsFragment";
    TagsAndCountersAdapter tagsAndCountersAdapter;
    ListView               lvTags;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context= getActivity().getApplicationContext();

        View root = inflater.inflate(R.layout.fragment_place_tags, container, false);

        //Create ListView
        //////////////////////////////////////////////////////////////////////////////
        //Find listview in XML
        lvTags = (ListView) root.findViewById(R.id.list_tags);

        initAdapter();
        loadTags();
        return root;
    }

    private void initAdapter() {
        ArrayList<Tag> data = new ArrayList<Tag>();
        tagsAndCountersAdapter = new TagsAndCountersAdapter(getActivity());
        tagsAndCountersAdapter.add(Tag.createDummy());
        lvTags.setAdapter(tagsAndCountersAdapter);
    }


    private void loadTags() {
        // TODO check that this is a PlaceActivity
        final PlaceActivity placeActivity = (PlaceActivity) getActivity();
        RestClient.service().viewPopularTagsForPlace(placeActivity.getPlace().id, new RestCallback<List<Tag>>(getContext()) {
            @Override
            public void success(List<Tag> tags, Response response) {
                notifyTagsLoaded(tags);
            }
        });
    }

    private void notifyTagsLoaded(List<Tag> tags) {
        // TODO jean
    }

    /*private ArrayList<Tag> generateDummyData() {
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
    }*/
}
