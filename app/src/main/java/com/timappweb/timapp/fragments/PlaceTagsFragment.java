package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.config.QuotaManager;
import com.timappweb.timapp.config.QuotaType;
import com.timappweb.timapp.data.entities.UserPlaceStatusEnum;
import com.timappweb.timapp.data.loader.MultipleEntryLoaderCallback;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.PlacesInvitation;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.rest.ApiCallFactory;
import com.timappweb.timapp.rest.RestCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.sync.DataSyncAdapter;
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
    private View                    noTagsView;
    private View                    noConnectionView;
    private EventView               eventView;
    private SwipeRefreshLayout      mSwipeLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        eventActivity = (EventActivity) getActivity();
        View root = inflater.inflate(R.layout.fragment_event_tags, container, false);

        //Find views
        rvTags = (ListView) root.findViewById(R.id.list_tags);
        //progressView = root.findViewById(R.id.progress_view);
        noTagsView = root.findViewById(R.id.no_tags_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);
        mSwipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout_place_tags);

        //Create Event View
        if(eventActivity.getEventToolbar().getVisibility()==View.VISIBLE) {
            eventView = new EventView(eventActivity, true);
        } else {
            eventView = new EventView(eventActivity);
        }

        eventView.setBottomShadow(true);
        eventView.setTagsVisible(false);
        eventView.setEvent(eventActivity.getEvent());
        FrameLayout eventFrameLayout = (FrameLayout) root.findViewById(R.id.event_frame_layout);
        eventFrameLayout.addView(eventView);
        eventFrameLayout.setVisibility(View.VISIBLE);

        tagsAndCountersAdapter = new TagsAndCountersAdapter(getActivity());
        rvTags.setAdapter(tagsAndCountersAdapter);

        getLoaderManager().initLoader(0, null, new PlaceTagLoader(this.getContext(), ((EventActivity) getActivity()).getEvent()));

        return root;
    }

    /*@Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
    }

*/
    // TODO WTFFFF
    public EventView getEventView() {
        return eventView;
    }


    // =============================================================================================
    /**
     * TODO
     */
    class PlaceTagLoader extends MultipleEntryLoaderCallback<Tag> {

        public PlaceTagLoader(Context context, Place place) {
            super(context, 3600 * 1000,
                    DataSyncAdapter.SYNC_TYPE_PLACE_TAGS,
                    place.getTagsQuery());

            this.syncOption.getBundle().putLong(DataSyncAdapter.SYNC_PARAM_PLACE_ID, place.getRemoteId());

            //this.setSwipeAndRefreshLayout(mSwipeLayout);
        }

        @Override
        public void onLoadFinished(Loader loader, List data) {
            super.onLoadFinished(loader, data);
            tagsAndCountersAdapter.clear();

            if (data.size() == 0){
                noTagsView.setVisibility(View.VISIBLE);
                rvTags.setVisibility(View.GONE);
            }
            else{
                /*
                for (Tag tag : tags) {
                    String addedhastag = "#" + tag.getName();
                    tag.setName(addedhastag);
                    tagsAndCountersAdapter.add(tag);
                }*/
                noTagsView.setVisibility(View.GONE);
                rvTags.setVisibility(View.VISIBLE);
                tagsAndCountersAdapter.addAll(data);
                tagsAndCountersAdapter.notifyDataSetChanged();
            }
        }

    }
}
