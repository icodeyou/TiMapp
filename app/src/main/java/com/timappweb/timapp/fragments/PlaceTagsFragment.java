package com.timappweb.timapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.cache.CacheData;
import com.timappweb.timapp.activities.PlaceActivity;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.entities.Place;
import com.timappweb.timapp.entities.Tag;
import com.timappweb.timapp.entities.UserPlaceStatus;
import com.timappweb.timapp.rest.QueryCondition;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.RestFeedbackCallback;
import com.timappweb.timapp.rest.model.RestFeedback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class PlaceTagsFragment extends Fragment {

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
    private View                    addButton;
    private View                    smallPicButton;
    private View                    smallPeopleButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        placeActivity = (PlaceActivity) getActivity();
        place = placeActivity.getPlace();
        placeId = placeActivity.getPlaceId();

        View root = inflater.inflate(R.layout.fragment_place_tags, container, false);

        //Initialize
        addButton = root.findViewById(R.id.main_button);
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
        if (visible) {
            if(addButton!=null) {
                boolean test = addButton.getVisibility()==View.VISIBLE;
                placeActivity.setPlusButtonVisibility(test);
            }
        }
        super.setMenuVisibility(visible);
    }

    private void initAdapter() {
        ArrayList<Tag> data = new ArrayList<Tag>();
        tagsAndCountersAdapter = new TagsAndCountersAdapter(getActivity());
        lvTags.setAdapter(tagsAndCountersAdapter);
    }

    private void setListeners() {

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO fine location
                if (!MyApplication.hasLastLocation()) {
                    Toast.makeText(getActivity(), R.string.error_cannot_get_location, Toast.LENGTH_LONG).show();
                    return;
                }

                QueryCondition conditions = new QueryCondition();
                conditions.setPlaceId(placeId);
                conditions.setAnonymous(false);
                conditions.setUserLocation(MyApplication.getLastLocation());
                Call<RestFeedback> call = RestClient.service().placeHere(conditions.toMap());
                call.enqueue(new RestFeedbackCallback(placeActivity) {
                    @Override
                    public void onActionSuccess(RestFeedback feedback) {
                        Log.d(TAG, "Success register here for user");
                    }

                    @Override
                    public void onActionFail(RestFeedback feedback) {
                        Log.d(TAG, "Fail register here for user");
                    }
                });

                IntentsUtils.addPostStepTags(placeActivity, place);
            }
        });
    }

    public void updateBtnVisibility() {
        Log.d(TAG, "PlaceActivity.updateButtonsVisibility()");

        // Check if the user can post in this place
        if (place != null && MyApplication.hasLastLocation() && CacheData.isAllowedToAddPost() && place.isAround()){
            addButton.setVisibility(View.VISIBLE);
        }
        else if (place != null && MyApplication.hasLastLocation() && CacheData.isAllowedToAddUserStatus(place.id, UserPlaceStatus.COMING)){
            addButton.setVisibility(View.GONE);
        }
        else {
            addButton.setVisibility(View.GONE);
        }
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

    public void setMainButtonVisibility(boolean bool) {
        if(bool) {
            addButton.setVisibility(View.VISIBLE);
        }
        else {
            addButton.setVisibility(View.GONE);
        }
    }

    public boolean getMainButtonVisibility() {
        if(addButton.getVisibility()==View.VISIBLE) {
            return true;
        }
        else {
            return false;
        }
    }

    public void setSmallPicButtonVisibility(boolean bool) {
        if(bool) {
            smallPicButton.setVisibility(View.VISIBLE);
        }
        else {
            smallPicButton.setVisibility(View.GONE);
        }
    }

    public void setSmallPeopleButtonVisibility(boolean bool) {
        if(bool) {
            smallPeopleButton.setVisibility(View.VISIBLE);
        }
        else {
            smallPeopleButton.setVisibility(View.GONE);
        }
    }

}
