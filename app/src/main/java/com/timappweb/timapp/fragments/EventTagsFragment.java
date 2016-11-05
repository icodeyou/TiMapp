package com.timappweb.timapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.PlaceHolderItem;
import com.timappweb.timapp.adapters.flexibleadataper.models.TagItem;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.RecyclerViewManager;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateDataLoader;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateManager;
import com.timappweb.timapp.data.loader.paginate.PaginateFilter;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventTag;
import com.timappweb.timapp.data.models.EventTag_Table;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.listeners.OnTabSelectedListener;
import com.timappweb.timapp.utils.DurationConstants;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;


public class EventTagsFragment extends EventBaseFragment implements OnTabSelectedListener{

    private static final String TAG = "EventTagsFragment";
    //private static final long MIN_DELAY_AUTO_REFRESH = 5 * 60 * 1000;
    private static final long MIN_DELAY_FORCE_REFRESH = 60 * 1000;
    private static final long MAX_CACHE_VALIDITY = DurationConstants.HOUR_MILLIS;

    // ---------------------------------------------------------------------------------------------

    //private TagsAndCountersAdapter          tagsAndCountersAdapter;

    //Views
    private View                            noTagsView;
    //private EventView                     eventView;
    private FloatingActionButton            postButton;
    private RecyclerView                    mRecyclerView;
    private MyFlexibleAdapter               mAdapter;
    private SwipeRefreshLayout              mSwipeRefreshLayout;
    private View                            placeHolder;
    public CursorPaginateManager<EventTag> mRecyclerViewManager;
    public CursorPaginateDataLoader<EventTag, Tag> mDataLoader;

    // ---------------------------------------------------------------------------------------------

    public EventTagsFragment() {
        setTitle(R.string.title_fragment_tags);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_event_tags, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_tags);
        noTagsView = view.findViewById(R.id.no_tags_view);
        placeHolder = view.findViewById(R.id.material_view_pager_placeholder);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_place_tags);
        //tagsAndCountersAdapter = new TagsAndCountersAdapter(getActivity());

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new MyFlexibleAdapter(getContext());
        mAdapter.beginningOffset = 1;
        mAdapter.addItem(new PlaceHolderItem("PLACEHOLDER_EVENT_TAGS_FRAGMENT"));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);

        mAdapter.initializeListeners(new FlexibleAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(int position) {
                AbstractFlexibleItem item = mAdapter.getItem(position);
                if (item instanceof TagItem){
                    IntentsUtils.checkAndAddTags(eventActivity, getEvent(), ((TagItem)item).getTag());
                }
                return true;
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
    }

    private void initDataLoader(){
        final Event event = getEvent();
        this.mDataLoader = CursorPaginateDataLoader.<EventTag, Tag>create(
                    "tags/event/" + getEvent().getRemoteId(),
                    Tag.class
                )
                .initCache("EventTag:" + getEvent().getRemoteId(), MAX_CACHE_VALIDITY)
                .setCacheCallback(new CursorPaginateDataLoader.CacheCallback<EventTag, Tag>() {
                    @Override
                    public EventTag beforeSaveModel(Tag tag) {
                        EventTag eventTag = new EventTag();
                        eventTag.event = getEvent();
                        eventTag.tag = tag;
                        eventTag.count_ref = tag.count_ref;
                        return eventTag;
                    }
                })
                .setLocalQuery(SQLite.select()
                        .from(EventTag.class)
                        .where(EventTag_Table.event_id.eq(event.id)))
                .setClearQuery(SQLite.delete()
                        .from(EventTag.class)
                        .where(EventTag_Table.event_id.eq(event.id)))
                .addFilter(new PaginateFilter(EventTag_Table.count_ref, "count_ref", false,
                        new CursorPaginateDataLoader.FilterValueTransformer<EventTag>() {
                            @Override
                            public Object transform(EventTag model) {
                                return model.count_ref;
                            }
                        }))
                .addFilter(new PaginateFilter(EventTag_Table.id, "id", false, new CursorPaginateDataLoader.FilterValueTransformer<EventTag>() {
                    @Override
                    public Object transform(EventTag model) {
                        return model.id;
                    }
                }))
                .enableCache(!MyApplication.isLowMemory());

        this.mRecyclerViewManager = new CursorPaginateManager<EventTag>(getContext(), mAdapter, mDataLoader)
                .setItemTransformer(new RecyclerViewManager.ItemTransformer<EventTag>() {
                    @Override
                    public AbstractFlexibleItem createItem(EventTag data) {
                        data.tag.count_ref = data.count_ref;
                        return new TagItem(data.tag);// new (InvitationsActivity.this, data);
                    }
                })
                .setMinDelayForceRefresh(MIN_DELAY_FORCE_REFRESH)
                .setNoDataView(noTagsView)
                .setSwipeRefreshLayout(mSwipeRefreshLayout)
                .enableEndlessScroll()
                .setClearOnRefresh(true)
                //.setCallback(this)
                .load();
    }

    private void loadDataIfNeeded() {
        if (mRecyclerViewManager != null || !isAdded()) return;
        initDataLoader();
        //this.mRecyclerViewManager.load();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case IntentsUtils.REQUEST_TAGS:
                if(resultCode == Activity.RESULT_OK) {
                    mRecyclerViewManager.clearItems();
                    mDataLoader.localLoad();
                }
                break;
            default:
                Log.e(TAG, "Unknown activity result: " + requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    public void onTabSelected() {
        if (mRecyclerView != null){
            mRecyclerView.smoothScrollToPosition(0);
        }
        loadDataIfNeeded();
    }

    @Override
    public void onTabUnselected() {

    }

    // ---------------------------------------------------------------------------------------------

}
