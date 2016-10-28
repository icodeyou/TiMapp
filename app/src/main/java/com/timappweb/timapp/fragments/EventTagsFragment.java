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

import com.activeandroid.query.Select;
import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.TagsAndCountersAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.PlaceHolderItem;
import com.timappweb.timapp.adapters.flexibleadataper.models.TagItem;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.RecyclerViewManager;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateDataLoader;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateManager;
import com.timappweb.timapp.data.models.EventTag;
import com.timappweb.timapp.data.models.MyModel;
import com.timappweb.timapp.data.models.Tag;
import com.timappweb.timapp.listeners.OnTabSelectedListener;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;


public class EventTagsFragment extends EventBaseFragment implements OnTabSelectedListener{

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
    private MyFlexibleAdapter mAdapter;
    private SwipeRefreshLayout              mSwipeRefreshLayout;
    private View                            placeHolder;
    private CursorPaginateManager<EventTag> mRecyclerViewManager;

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
        tagsAndCountersAdapter = new TagsAndCountersAdapter(getActivity());

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new MyFlexibleAdapter(getContext());
        mAdapter.removeAllOffset = 1;
        mAdapter.addItem(new PlaceHolderItem("PLACEHOLDER_EVENT_TAGS_FRAGMENT"));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);

        mAdapter.initializeListeners(new FlexibleAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(int position) {
                IntentsUtils.checkAndAddTags(eventActivity, eventActivity.getEvent(), tagsAndCountersAdapter.getTag(position));
                return true;
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
    }

    private void initDataLoader(){
        CursorPaginateDataLoader<EventTag, Tag> mDataLoader = CursorPaginateDataLoader.<EventTag, Tag>create(
                    "tags/event/" + getEvent().getRemoteId(),
                    Tag.class
                )
                .initCache("EventTag:" + getEvent().getRemoteId(), 0) // never expire
                .setCacheCallback(new CursorPaginateDataLoader.CacheCallback<EventTag, Tag>() {
                    @Override
                    public EventTag beforeSaveModel(Tag tag) {
                        EventTag eventTag = new EventTag();
                        eventTag.event = getEvent();
                        eventTag.tag = tag;
                        return eventTag;
                    }
                })
                .setLocalQuery(new Select()
                        .from(EventTag.class)
                        .where("EventTag.Event = ?", getEvent().getId()))
                .addFilter(new CursorPaginateDataLoader.PaginateFilter("CountRef", "count_ref", CursorPaginateDataLoader.PaginateFilter.DESC,
                        new CursorPaginateDataLoader.FilterValueTransformer<EventTag>() {
                            @Override
                            public Object transform(EventTag model) {
                                return model.count_ref;
                            }
                        }))
                .addFilter(new CursorPaginateDataLoader.PaginateFilter("id", "id", CursorPaginateDataLoader.PaginateFilter.DESC, new CursorPaginateDataLoader.FilterValueTransformer<MyModel>() {
                    @Override
                    public Object transform(MyModel model) {
                        return model.getId();
                    }
                }));

        this.mRecyclerViewManager = new CursorPaginateManager<EventTag>(getContext(), mAdapter, mDataLoader)
                .setItemTransformer(new RecyclerViewManager.ItemTransformer<EventTag>() {
                    @Override
                    public AbstractFlexibleItem createItem(EventTag data) {
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
