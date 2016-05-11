package com.timappweb.timapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.rest.ApiCallFactory;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.views.EventView;
import com.timappweb.timapp.views.EventView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class PlaceTagsFragment extends PlaceBaseFragment {

    private static final String TAG = "PlaceTagsFragment";
    private TagsAndCountersAdapter  tagsAndCountersAdapter;
    private EventActivity eventActivity;

    //Views
    private ListView                rvTags;
    private View                    progressView;
    private View                    noTagsView;
    private View                    noConnectionView;
    private EventView               eventView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        eventActivity = (EventActivity) getActivity();
        View root = inflater.inflate(R.layout.fragment_event_tags, container, false);

        //Find views
        rvTags = (ListView) root.findViewById(R.id.list_tags);
        progressView = root.findViewById(R.id.progress_view);
        noTagsView = root.findViewById(R.id.no_tags_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);

        //Create Event View
        if(eventActivity.getEventToolbar().getVisibility()==View.VISIBLE) {
            eventView = new EventView(eventActivity, true);
        } else {
            eventView = new EventView(eventActivity);
        }

        eventView.setBottomShadow(true);
        eventView.setTagsVisible(false);
        eventView.setEvent(eventActivity.getEvent());
        eventActivity.updateBtnVisibility();
        FrameLayout eventFrameLayout = (FrameLayout) root.findViewById(R.id.event_frame_layout);
        eventFrameLayout.addView(eventView);
        eventFrameLayout.setVisibility(View.VISIBLE);

        initAdapter();
        loadData();

        return root;
    }

    /*@Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
    }
*/
    public EventView getEventView() {
        return eventView;
    }

    private void initAdapter() {
        ArrayList<Tag> data = new ArrayList<Tag>();
        tagsAndCountersAdapter = new TagsAndCountersAdapter(getActivity());
        rvTags.setAdapter(tagsAndCountersAdapter);
    }

    public void loadData() {
        final EventActivity eventActivity = (EventActivity) getActivity();
        Call<List<Tag>> call = RestClient.service().viewPopularTagsForPlace(eventActivity.getEventId());
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
}
