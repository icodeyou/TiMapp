package com.timappweb.timapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.database.models.QuotaType;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.rest.ApiCallFactory;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class PlaceTagsFragment extends PlaceBaseFragment {

    private static final String TAG = "PlaceTagsFragment";
    private TagsAndCountersAdapter  tagsAndCountersAdapter;
    private PlaceActivity placeActivity;

    //Views
    private ListView                rvTags;
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
        View root = inflater.inflate(R.layout.fragment_place_tags, container, false);

        //Initialize
        mainButton = root.findViewById(R.id.main_button);
        tvAddButton = (TextView) root.findViewById(R.id.text_main_button);
        smallPicButton = root.findViewById(R.id.button_add_pic);
        smallPeopleButton = root.findViewById(R.id.button_add_people);
        rvTags = (ListView) root.findViewById(R.id.list_tags);
        progressView = root.findViewById(R.id.progress_view);
        noTagsView = root.findViewById(R.id.no_tags_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);

        initAdapter();
        setListeners();
        loadData();


        updateBtnVisibility();

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
        rvTags.setAdapter(tagsAndCountersAdapter);
    }

    private void setListeners() {
        mainButton.setOnClickListener(placeActivity.getTagListener());
        smallPicButton.setOnClickListener(placeActivity.getPictureListener());
        smallPeopleButton.setOnClickListener(placeActivity.getPeopleListener());
    }

    public void loadData() {
        final PlaceActivity placeActivity = (PlaceActivity) getActivity();
        Call<List<Tag>> call = RestClient.service().viewPopularTagsForPlace(placeActivity.getPlaceId());
        RestCallback callback = new RestCallback<List<Tag>>(getContext(), this) {
            @Override
            public void onResponse(Response<List<Tag>> response) {
                super.onResponse(response);
                if (response.isSuccess()) {
                    notifyTagsLoaded(response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                super.onFailure(t);
                noConnectionView.setVisibility(View.VISIBLE);
            }
        };

        asynCalls.add(ApiCallFactory.build(call, callback, this));
    }

    private void notifyTagsLoaded(List<Tag> tags) {
        tagsAndCountersAdapter.clear();
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

    public void setProgressView(boolean visibility) {
        if(visibility) {
            progressView.setVisibility(View.VISIBLE);
            rvTags.setVisibility(View.GONE);
            noConnectionView.setVisibility(View.GONE);
        } else {
            progressView.setVisibility(View.GONE);
            rvTags.setVisibility(View.VISIBLE);
            noConnectionView.setVisibility(View.GONE);
        }
    }

    public void updateBtnVisibility() {
        Log.v(TAG, "::updateBtnVisibility()");
        // Check if the user can post in this place
        boolean isUserAround = placeActivity.isUserAround();
        boolean isAllowedToAddPic = QuotaManager.instance().checkQuota(QuotaType.PICTURE);
        boolean isAllowedToAddPost = QuotaManager.instance().checkQuota(QuotaType.POST);
        boolean isAllowedToAddPeople = QuotaManager.instance().checkQuota(QuotaType.FRIENDS);
        boolean showMainButton = isUserAround && isAllowedToAddPost;
        mainButton.setVisibility(showMainButton ? View.VISIBLE : View.GONE);
        smallPeopleButton.setVisibility(isUserAround && !showMainButton && isAllowedToAddPeople ? View.VISIBLE : View.GONE);
        smallPicButton.setVisibility(isUserAround && !showMainButton && isAllowedToAddPic ? View.VISIBLE : View.GONE);
    }

}
