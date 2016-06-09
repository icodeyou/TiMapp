package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;
import com.google.android.gms.maps.LocationSource;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.MultipleEntryLoaderCallback;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventTag;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.utils.location.LocationManager;
import com.timappweb.timapp.views.RefreshableRecyclerView;

import java.util.List;


public class EventTagsFragment extends EventBaseFragment implements LocationManager.LocationListener {

    private static final String TAG = "EventTagsFragment";
    private static final long MAX_UPDATE_DELAY = 3600 * 1000;
    private TagsAndCountersAdapter  tagsAndCountersAdapter;

    //Views
    private View                    noTagsView;
    //private EventView               eventView;
    private SwipeRefreshLayout      mSwipeLayout;
    private FloatingActionButton postButton;
    private RecyclerView mRecyclerView;
    private RecyclerViewMaterialAdapter mAdapter;
    private Loader<List<EventTag>> mTagLoader;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_event_tags, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Find views
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_tags);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);

        noTagsView = view.findViewById(R.id.no_tags_view);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_place_tags);
        postButton = (FloatingActionButton) view.findViewById(R.id.post_button);

        tagsAndCountersAdapter = new TagsAndCountersAdapter(getActivity());
        mAdapter = new RecyclerViewMaterialAdapter(tagsAndCountersAdapter);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);


        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            IntentsUtils.addTags(EventTagsFragment.this, eventActivity.getEvent());
            }
        });

        mTagLoader = getLoaderManager()
                .initLoader(EventActivity.LOADER_ID_TAGS, null, new PlaceTagLoader(this.getContext(), ((EventActivity) getActivity()).getEvent()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case IntentsUtils.REQUEST_TAGS:
                if(resultCode == Activity.RESULT_OK) {
                    // TODO update list
                    //mTagLoader.reset();
                }
                break;
            default:
                Log.e(TAG, "Unknown activity result: " + requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocationManager.addOnLocationChangedListener(this);
    }
    @Override
    public void onPause() {
        super.onResume();
        LocationManager.removeLocationListener(this);
    }

    @Override
    public void onLocationChanged(Location newLocation, Location lastLocation) {
        postButton.setVisibility(eventActivity.isUserAround() ? View.VISIBLE : View.GONE);
    }

    // =============================================================================================
    /**
     * TODO
     */
    class PlaceTagLoader extends MultipleEntryLoaderCallback<EventTag> {

        public PlaceTagLoader(Context context, Event event) {
            super(context, MAX_UPDATE_DELAY,
                    DataSyncAdapter.SYNC_TYPE_EVENT_TAGS,
                    event.getTagsQuery());

            this.syncOption.getBundle().putLong(DataSyncAdapter.SYNC_PARAM_EVENT_ID, event.getRemoteId());
            this.setSwipeAndRefreshLayout(mSwipeLayout);
        }

        @Override
        public void onLoadFinished(Loader<List<EventTag>> loader, List<EventTag> data) {
            super.onLoadFinished(loader, data);
            tagsAndCountersAdapter.clear();
            tagsAndCountersAdapter.addAll(data);
            mAdapter.notifyDataSetChanged();
            noTagsView.setVisibility(data.size() == 0 ? View.VISIBLE : View.GONE);
        }

    }
}