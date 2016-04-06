package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLngBounds;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.rest.model.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class ExploreTagsFragment extends BaseFragment {

    private static final String TAG = "PlaceTagsFragment";
    TagsAndCountersAdapter placesAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context= getActivity().getApplicationContext();

        View root = inflater.inflate(R.layout.fragment_explore_places, container, false);

        //Initialize variables
        ListView lvTags = (ListView) root.findViewById(R.id.list_tags);

        placesAdapter = new TagsAndCountersAdapter(context);
        lvTags.setAdapter(placesAdapter);


        return root;
    }

    public void onResume(){
        super.onResume();
        Log.d(TAG, "PlaceTagsFragment.onResume()");
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

        Call<List<Tag>> call = RestClient.service().trendingTags(conditions.toMap());

        call.enqueue(new RestCallback<List<Tag>>(getContext()) {
            @Override
            public void onResponse(Response<List<Tag>> response) {
                super.onResponse(response);
                if (response.isSuccess()){
                    List<Tag> tags = response.body();
                    Log.i(TAG, "Updating list with " + tags.size() + " items");
                    placesAdapter.clear();
                    placesAdapter.addAll(tags);
                }
            }
        });

        asynCalls.add(call);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "ExploreTagsFragment::onPause() -> cancelling api calls");
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
