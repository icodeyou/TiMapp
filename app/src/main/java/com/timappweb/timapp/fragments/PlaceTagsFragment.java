package com.timappweb.timapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.cache.CacheData;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class PlaceTagsFragment extends BaseFragment {

    private static final String TAG = "PlaceTagsFragment";
    private TagsAndCountersAdapter  tagsAndCountersAdapter;
    private PlaceActivity placeActivity;
    private Place place;
    private int placeId;

    //Views
    private ListView                lvTags;
    private View                    progressView;
    private View                    noTagsView;
    private View                    noConnectionView;
    private View mainButton;
    private View                    smallPicButton;
    private View                    smallPeopleButton;
    private TextView                tvAddButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        placeActivity = (PlaceActivity) getActivity();
        place = placeActivity.getPlace();
        placeId = placeActivity.getPlaceId();

        View root = inflater.inflate(R.layout.fragment_place_tags, container, false);

        //Initialize
        mainButton = root.findViewById(R.id.main_button);
        tvAddButton = (TextView) root.findViewById(R.id.text_main_button);
        smallPicButton = root.findViewById(R.id.button_add_pic);
        smallPeopleButton = root.findViewById(R.id.button_add_people);
        lvTags = (ListView) root.findViewById(R.id.list_tags);
        progressView = root.findViewById(R.id.progress_view);
        noTagsView = root.findViewById(R.id.no_tags_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);

        initAdapter();
        setListeners();
        loadTags();

        placeActivity.notifyFragmentsLoaded();

        //Call setMenuVisibility to update Plus Button visibility
        //setMenuVisibility(true);

        return root;
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
    }

    private void initAdapter() {
        ArrayList<Tag> data = new ArrayList<Tag>();
        tagsAndCountersAdapter = new TagsAndCountersAdapter(getActivity());
        lvTags.setAdapter(tagsAndCountersAdapter);
    }

    private void setListeners() {
        mainButton.setOnClickListener(placeActivity.getTagListener());
        smallPicButton.setOnClickListener(placeActivity.getPictureListener());
        smallPeopleButton.setOnClickListener(placeActivity.getPeopleListener());
    }

    private void loadTags() {
        final PlaceActivity placeActivity = (PlaceActivity) getActivity();
        Call<List<Tag>> call = RestClient.service().viewPopularTagsForPlace(placeActivity.getPlaceId());
        call.enqueue(new RestCallback<List<Tag>>(getContext()) {
            @Override
            public void onResponse(Response<List<Tag>> response) {
                super.onResponse(response);
                if (response.isSuccess()) {
                    progressView.setVisibility(View.GONE);
                    notifyTagsLoaded(response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                progressView.setVisibility(View.GONE);
                noConnectionView.setVisibility(View.VISIBLE);
            }
        });
        asynCalls.add(call);
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


    public View getMainButton() {
        return mainButton;
    }

    public TextView getTvMainButton() {
        return tvAddButton;
    }



    public void updateBtnVisibility() {
        Log.v(TAG, "::updateButtonsVisibility()");
        // Check if the user can post in this place
        boolean showMainButton = place != null && MyApplication.hasLastLocation() && CacheData.isAllowedToAddPost() && place.isAround();
        mainButton.setVisibility(showMainButton ? View.VISIBLE : View.GONE);
        smallPeopleButton.setVisibility(place != null && !showMainButton && place.isAround() ? View.VISIBLE : View.GONE);
        smallPicButton.setVisibility(place != null && !showMainButton && place.isAround() && CacheData.isAllowedToAddPicture() ? View.VISIBLE : View.GONE);
    }

}
