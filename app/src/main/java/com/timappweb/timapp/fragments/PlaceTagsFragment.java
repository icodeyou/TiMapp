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

import retrofit2.Call;
import retrofit2.Response;


public class PlaceTagsFragment extends Fragment {

    private static final String TAG = "PlaceTagsFragment";
    private TagsAndCountersAdapter  tagsAndCountersAdapter;
    private ListView                lvTags;
    private View                    progressView;
    private View                    noTagsView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context= getActivity().getApplicationContext();

        View root = inflater.inflate(R.layout.fragment_place_tags, container, false);

        //Initialize
        lvTags = (ListView) root.findViewById(R.id.list_tags);
        progressView = root.findViewById(R.id.progress_view);
        noTagsView = root.findViewById(R.id.no_tags_view);

        initAdapter();
        loadTags();
        return root;
    }

    private void initAdapter() {
        ArrayList<Tag> data = new ArrayList<Tag>();
        tagsAndCountersAdapter = new TagsAndCountersAdapter(getActivity());
        lvTags.setAdapter(tagsAndCountersAdapter);
    }


    private void loadTags() {
        final PlaceActivity placeActivity = (PlaceActivity) getActivity();
        Call<List<Tag>> call = RestClient.service().viewPopularTagsForPlace(placeActivity.getPlaceId());
        call.enqueue(new RestCallback<List<Tag>>(getContext()) {
            @Override
            public void onResponse(Response<List<Tag>> response) {
                super.onResponse(response);
                if (response.isSuccess()){
                    progressView.setVisibility(View.GONE);
                    notifyTagsLoaded(response.body());
                }
            }

        });
    }

    private void notifyTagsLoaded(List<Tag> tags) {
        //add tags to adapter
        for (Tag tag : tags) {
            String addedhastag = "#" + tag.getName();
            tag.setName(addedhastag);
            tagsAndCountersAdapter.add(tag);
        }
        if(tags.isEmpty()) {
            noTagsView.setVisibility(View.VISIBLE);
        }
    }
}
