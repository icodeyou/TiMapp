package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.data.loader.MultipleEntryLoaderCallback;
import com.timappweb.timapp.data.models.Place;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.sync.DataSyncAdapter;
import com.timappweb.timapp.views.EventView;
import com.timappweb.timapp.views.RefreshableRecyclerView;

import java.util.List;


public class EventTagsFragment extends EventBaseFragment {

    private static final String TAG = "EventTagsFragment";
    private static final long MAX_UPDATE_DELAY = 10 * 1000;
    private TagsAndCountersAdapter  tagsAndCountersAdapter;
    private EventActivity eventActivity;

    //Views
    private RefreshableRecyclerView rvTags;
    private View                    noTagsView;
    private View                    noConnectionView;
    //private EventView               eventView;
    private SwipeRefreshLayout      mSwipeLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        eventActivity = (EventActivity) getActivity();
        View root = inflater.inflate(R.layout.fragment_event_tags, container, false);

        //Find views
        rvTags = (RefreshableRecyclerView) root.findViewById(R.id.list_tags);
        rvTags.setLayoutManager(new LinearLayoutManager(getContext()));

        //progressView = root.findViewById(R.id.progress_view);
        noTagsView = root.findViewById(R.id.no_tags_view);
        noConnectionView = root.findViewById(R.id.no_connection_view);
        mSwipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout_place_tags);

        tagsAndCountersAdapter = new TagsAndCountersAdapter(getActivity());
        rvTags.setAdapter(tagsAndCountersAdapter);

        getLoaderManager().initLoader(EventActivity.LOADER_ID_TAGS, null, new PlaceTagLoader(this.getContext(), ((EventActivity) getActivity()).getEvent()));
        return root;
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

        public PlaceTagLoader(Context context, Place place) {
            super(context, MAX_UPDATE_DELAY,
                    DataSyncAdapter.SYNC_TYPE_PLACE_TAGS,
                    place.getTagsQuery());

            this.syncOption.getBundle().putLong(DataSyncAdapter.SYNC_PARAM_PLACE_ID, place.getRemoteId());
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
