package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.MultipleEntryLoaderCallback;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.views.RefreshableRecyclerView;
import com.timappweb.timapp.views.parallaxviewpager.RecyclerViewFragment;

import java.util.List;


public class EventTagsFragment extends EventBaseFragment {

    private static final String TAG = "EventTagsFragment";
    private static final long MAX_UPDATE_DELAY = 10 * 1000;
    private TagsAndCountersAdapter  tagsAndCountersAdapter;
    private EventActivity eventActivity;

    //Views
    private View                    noTagsView;
    private View                    noConnectionView;
    //private EventView               eventView;
    private SwipeRefreshLayout      mSwipeLayout;
    private FloatingActionButton postButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        eventActivity = (EventActivity) getActivity();
        View root = inflater.inflate(R.layout.fragment_event_tags, container, false);

        //Find views
        mRecyclerView = (RefreshableRecyclerView) root.findViewById(R.id.list_tags);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //progressView = root.findViewById(R.id.progress_view);
        noTagsView = root.findViewById(R.id.no_tags_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);
        mSwipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout_place_tags);
        postButton = (FloatingActionButton) root.findViewById(R.id.post_button);

        tagsAndCountersAdapter = new TagsAndCountersAdapter(getActivity());
        mRecyclerView.setAdapter(tagsAndCountersAdapter);


        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentsUtils.postEvent(getContext(), eventActivity.getEvent(), IntentsUtils.ACTION_TAGS);
            }
        });

        setupRecyclerView();

        getLoaderManager().initLoader(EventActivity.LOADER_ID_TAGS, null, new PlaceTagLoader(this.getContext(), ((EventActivity) getActivity()).getEvent()));
        return root;
    }



    public void onAddTagsClick(View view) {
        IntentsUtils.postEvent(getContext(), eventActivity.getEvent(), IntentsUtils.ACTION_TAGS);
    }

    // =============================================================================================
    // PARALLAX VIEW
    @Override
    protected void setScrollOnLayoutManager(int scrollY) {
        ((LinearLayoutManager)mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(0, -scrollY);
    }

    @Override
    protected void setupRecyclerView() {
        setRecyclerViewOnScrollListener();
    }

    /*@Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
    }
*/
    // =============================================================================================
    /**
     * TODO
     */
    class PlaceTagLoader extends MultipleEntryLoaderCallback<Tag> {

        public PlaceTagLoader(Context context, Event event) {
            super(context, MAX_UPDATE_DELAY,
                    DataSyncAdapter.SYNC_TYPE_EVENT_TAGS,
                    event.getTagsQuery());

            this.syncOption.getBundle().putLong(DataSyncAdapter.SYNC_PARAM_EVENT_ID, event.getRemoteId());
            this.setSwipeAndRefreshLayout(mSwipeLayout);
        }

        @Override
        public void onLoadFinished(Loader loader, List data) {
            super.onLoadFinished(loader, data);
            tagsAndCountersAdapter.clear();
            tagsAndCountersAdapter.addAll(data);
            tagsAndCountersAdapter.notifyDataSetChanged();
            noTagsView.setVisibility(data.size() == 0 ? View.VISIBLE : View.GONE);
        }

    }
}
