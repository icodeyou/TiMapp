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
import com.timappweb.timapp.adapters.ListTagAdapter;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.ArrayList;
import java.util.List;

import retrofit.client.Response;

public class ExploreTagsFragment extends Fragment {

    private static final String TAG = "ExploreTagsFragment";
    ListTagAdapter mListTagAdapter;

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
        mListTagAdapter = new ListTagAdapter(context, new ArrayList<Tag>());
        lvTags.setAdapter(mListTagAdapter);

        return root;
    }

    public void onResume(){
        super.onResume();
        Log.d(TAG, "ExploreTagsFragment.onResume()");
        // TODO check if bounds have changed
        this.loadData();
    }


    private void loadData() {
        QueryCondition conditions = new QueryCondition();
        LatLngBounds bounds = ExploreMapFragment.getMapBounds();
        if (bounds == null){
            // TODO take care of this case
            Log.d(TAG, "There is no bound registered");
            return ;
        }
        conditions.setBounds(bounds);
        conditions.setTimeRange(ExploreMapFragment.getDataTimeRange());

        RestClient.service().trendingTags(conditions.toMap(), new RestCallback<List<Tag>>(getContext()) {
            @Override
            public void success(List<Tag> tags, Response response) {
                Log.i(TAG, "Updating list with " + tags.size() + " items");
                mListTagAdapter.clear();
                mListTagAdapter.addAll(tags);
            }
        });
    }

    private ArrayList<Tag> generateDummyData() {
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
