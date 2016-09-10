package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.SyncDataLoader;
import com.timappweb.timapp.data.models.EventTag;
import com.timappweb.timapp.listeners.OnItemAdapterClickListener;
import com.timappweb.timapp.listeners.OnTabSelectedListener;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.utils.loaders.AutoModelLoader;
import com.timappweb.timapp.utils.location.LocationManager;

import java.util.List;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;


public class EventTagsFragment extends EventBaseFragment implements LocationManager.LocationListener, OnTabSelectedListener, SyncDataLoader.Callback<EventTag> {

    private static final String TAG = "EventTagsFragment";
    private static final long MIN_DELAY_AUTO_REFRESH = 5 * 60 * 1000;
    private static final long MIN_DELAY_FORCE_REFRESH = 60 * 1000;

    // ---------------------------------------------------------------------------------------------

    private TagsAndCountersAdapter          tagsAndCountersAdapter;

    //Views
    private View                            noTagsView;
    //private EventView                     eventView;
    private FloatingActionButton            postButton;
    private RecyclerView                    mRecyclerView;
    private RecyclerViewMaterialAdapter     mAdapter;
    private Loader<List<EventTag>>          mTagLoader;
    private WaveSwipeRefreshLayout mSwipeRefreshLayout;
    private EventTagLoader eventTagLoader;

    // ---------------------------------------------------------------------------------------------

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
        mSwipeRefreshLayout = (WaveSwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_place_tags);
        mSwipeRefreshLayout.setWaveColor(ContextCompat.getColor(getContext(),R.color.colorRefresh));

        tagsAndCountersAdapter = new TagsAndCountersAdapter(getActivity());
        tagsAndCountersAdapter.setItemAdapterClickListener(new OnItemAdapterClickListener() {
            @Override
            public void onClick(int position) {
                IntentsUtils.postEvent(eventActivity, eventActivity.getEvent(), IntentsUtils.ACTION_TAGS);
            }
        });
        mAdapter = new RecyclerViewMaterialAdapter(tagsAndCountersAdapter);
        mRecyclerView.setAdapter(mAdapter);

        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);

        initEventTagLoader();

        mTagLoader = getLoaderManager()
                .initLoader(EventActivity.LOADER_ID_TAGS, null, eventTagLoader);
        mSwipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mTagLoader.forceLoad();
            }
        });
    }

    private void initEventTagLoader() {
        eventTagLoader = new EventTagLoader(this.getContext())
                .setMinDelayAutoRefresh(MIN_DELAY_FORCE_REFRESH)
                .setMinDelayForceRefresh(MIN_DELAY_AUTO_REFRESH)
                .setModelLoader(new AutoModelLoader<EventTag>(
                        this.getContext(),
                        EventTag.class,
                        getEvent().getTagsQuery(),
                        false))
                .setCallback(this);
        eventTagLoader.getSyncOptions().setType(DataSyncAdapter.SYNC_TYPE_EVENT_TAGS);
    }

    @Override
    public void onStart() {
        super.onStart();
        mTagLoader.forceLoad();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case IntentsUtils.REQUEST_TAGS:
                if(resultCode == Activity.RESULT_OK) {

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
        //postButton.setVisibility(eventActivity.isUserAround() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoadEnd(List<EventTag> data) {
        tagsAndCountersAdapter.clear();
        tagsAndCountersAdapter.addAll(data);
        mAdapter.notifyDataSetChanged();
        noTagsView.setVisibility(data.size() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoadError(Throwable error) {

    }

    @Override
    public void onTabSelected() {
        if (mRecyclerView != null){
            mRecyclerView.smoothScrollToPosition(0);
        }
    }

    // =============================================================================================
    /**
     */
    class EventTagLoader extends SyncDataLoader<EventTag, EventTagLoader> {

        public EventTagLoader(Context context) {
            super(context);
        }
    }

}
