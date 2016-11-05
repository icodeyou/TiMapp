package com.timappweb.timapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.PlaceHolderItem;
import com.timappweb.timapp.adapters.flexibleadataper.models.PeopleHeaderItem;
import com.timappweb.timapp.adapters.flexibleadataper.models.SubUserItem;
import com.timappweb.timapp.data.entities.UserEventStatusEnum;
import com.timappweb.timapp.data.loader.RecyclerViewManager;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateDataLoader;
import com.timappweb.timapp.data.loader.paginate.CursorPaginateManager;
import com.timappweb.timapp.data.loader.paginate.PaginateFilterFactory;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.EventsInvitation_Table;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.data.models.UserEvent_Table;
import com.timappweb.timapp.listeners.OnTabSelectedListener;
import com.timappweb.timapp.utils.DurationConstants;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import java.util.concurrent.atomic.AtomicInteger;

import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;


public class EventPeopleFragment extends EventBaseFragment implements OnTabSelectedListener, android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener {

    private static final String     TAG                             = "EventTagsFragment";
    private static final long       MIN_DELAY_FORCE_REFRESH_INVITE = DurationConstants.MINUTE_MILLIS;
    private static final int        LOCAL_LOAD_LIMIT                = 15;
    private static final long       CACHE_VALIDITY_DELAY_INVITE = DurationConstants.MINUTE_MILLIS * 15;
    // ---------------------------------------------------------------------------------------------

    private MyFlexibleAdapter       mPlaceUsersAdapter;
    private SwipeRefreshLayout      mSwipeLayout;
    private RecyclerView            mRecyclerView;
    private AtomicInteger loadCounter;

    private PeopleHeaderItem    mExpandableHereHeader;
    private PeopleHeaderItem    mExpandableComingHeader;
    private PeopleHeaderItem    mExpandableInviteHeader;

    private CursorPaginateManager<UserEvent> comingManager;
    private CursorPaginateManager<UserEvent> hereManager;
    private CursorPaginateManager<EventsInvitation> inviteSentManager;

    //private RecyclerViewMaterialAdapter mAdapter;

    // ---------------------------------------------------------------------------------------------

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventPeopleFragment() {
        setTitle(R.string.title_fragment_people);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_event_people, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_place_people);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_people);
        mSwipeLayout.setOnRefreshListener(this);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPlaceUsersAdapter = new MyFlexibleAdapter(getActivity());
        mPlaceUsersAdapter.setPermanentDelete(true);

        if (MyApplication.isLoggedIn()){
            mExpandableInviteHeader = new PeopleHeaderItem("INVITE", getResources().getString(R.string.header_invited));
            mPlaceUsersAdapter.addSection(mExpandableInviteHeader);
            inviteSentManager = initInviteSentLoader();
        }

        if (!getEvent().isOver()) {
            mExpandableComingHeader = new PeopleHeaderItem("COMING", getResources().getString(R.string.header_coming));
            mPlaceUsersAdapter.addSection(mExpandableComingHeader);
            comingManager = initUserStatusLoader(UserEventStatusEnum.COMING, mExpandableComingHeader);
        }

        if (getEvent().getVisibilityStatus() != Event.VisiblityStatus.PLANNED){
            int resourceId = getEvent().isOver() ? R.string.header_were_here : R.string.header_here;
            mExpandableHereHeader = new PeopleHeaderItem("HERE", getResources().getString(resourceId));
            mPlaceUsersAdapter.addSection(mExpandableHereHeader);
            hereManager = initUserStatusLoader(UserEventStatusEnum.HERE, mExpandableHereHeader);
        }

        mPlaceUsersAdapter.addItem(0, new PlaceHolderItem("PLACEHOLDER0"));
        initializeRecyclerView(savedInstanceState);
    }

    private CursorPaginateManager<UserEvent> initUserStatusLoader(UserEventStatusEnum status, final PeopleHeaderItem headerItem) {

        CursorPaginateDataLoader<UserEvent,UserEvent> dataLoader = CursorPaginateDataLoader.<UserEvent,UserEvent>create(
                    "PlacesUsers/event/" + getEvent().getRemoteId(),
                        UserEvent.class
                )
                .addQueryParam("status", status.toString().toLowerCase())
                .initCache("PlacesUsers:" + status.toString() + ":" + getEvent().getRemoteId(), 3600 * 1000) // never expire
                .setCacheCallback(new CursorPaginateDataLoader.CacheCallback<UserEvent, UserEvent>() {
                    @Override
                    public UserEvent beforeSaveModel(UserEvent model) {
                        model.event = getEvent();
                        return model;
                    }
                })
                .setLocalQuery(SQLite.select().from(UserEvent.class).where(UserEvent_Table.event_id.eq(getEvent().id)).and(UserEvent_Table.status.eq(status)))
                .setClearQuery(SQLite.delete().from(UserEvent.class).where(UserEvent_Table.event_id.eq(getEvent().id)).and(UserEvent_Table.status.eq(status)))
                .addFilter(PaginateFilterFactory.createCreatedFilter(UserEvent_Table.created))
                .addFilter(PaginateFilterFactory.createSyncIdFilter(UserEvent_Table.id))
                .setLimit(LOCAL_LOAD_LIMIT)
                .enableCache(false);


        return new CursorPaginateManager<UserEvent>(getContext(), mPlaceUsersAdapter, dataLoader)
                .setSubSection(headerItem)
                .setItemTransformer(new RecyclerViewManager.ItemTransformer<UserEvent>() {
                    @Override
                    public AbstractFlexibleItem createItem(UserEvent userEvent) {
                        return new SubUserItem(userEvent.status.toString()+ "-" + String.valueOf(userEvent.getRemoteId()), userEvent.getUser());
                    }
                })
                .setClearOnRefresh(true)
                .setMinDelayForceRefresh(MIN_DELAY_FORCE_REFRESH_INVITE)
                .setCallback(new CursorPaginateDataLoader.Callback<UserEvent>() {
                    @Override
                    public void onLoadEnd(CursorPaginateDataLoader.LoadInfo<UserEvent> data, CursorPaginateDataLoader.LoadType type, boolean overwrite) {
                        mPlaceUsersAdapter.expand(headerItem);
                        if (data != null && data.total != -1) headerItem.setCount(data.total, mPlaceUsersAdapter);
                        notifyLoadsEnd(type);
                    }

                    @Override
                    public void onLoadError(Throwable error, CursorPaginateDataLoader.LoadType type) {
                        notifyLoadsEnd(type);
                    }

                    @Override
                    public void onLoadStart(CursorPaginateDataLoader.LoadType type) {}
                });
                //.setSwipeRefreshLayout(mSwipeRefreshLayout)
                //.enableEndlessScroll()

    }

    private CursorPaginateManager<EventsInvitation> initInviteSentLoader() {
        CursorPaginateDataLoader<EventsInvitation, EventsInvitation> dataLoader = CursorPaginateDataLoader.<EventsInvitation, EventsInvitation>create(
                    "PlacesInvitations/sent/" + getEvent().getRemoteId(),
                    EventsInvitation.class
                )
                .initCache("PlacesInvitationsSent:" + getEvent().getRemoteId(), CACHE_VALIDITY_DELAY_INVITE) // never expire
                .setCacheCallback(new CursorPaginateDataLoader.CacheCallback<EventsInvitation, EventsInvitation>() {
                    @Override
                    public EventsInvitation beforeSaveModel(EventsInvitation model) {
                        model.event = getEvent();
                        model.user_source = MyApplication.getCurrentUser();
                        return model;
                    }
                })
                .setClearQuery(SQLite.delete().from(EventsInvitation.class)
                        .where(EventsInvitation_Table.event_id.eq(getEvent().id)).and(EventsInvitation_Table.user_source_id.eq(MyApplication.getCurrentUser().id)))
                .setLocalQuery(SQLite.select().from(EventsInvitation.class)
                        .where(EventsInvitation_Table.event_id.eq(getEvent().id)).and(EventsInvitation_Table.user_source_id.eq(MyApplication.getCurrentUser().id)))
                .addFilter(PaginateFilterFactory.createCreatedFilter(EventsInvitation_Table.created))
                .addFilter(PaginateFilterFactory.createSyncIdFilter(EventsInvitation_Table.id))
                .setLimit(LOCAL_LOAD_LIMIT)
                .enableCache(!MyApplication.isLowMemory());

        return new CursorPaginateManager<EventsInvitation>(getContext(), mPlaceUsersAdapter, dataLoader)
                .setSubSection(mExpandableInviteHeader)
                .setItemTransformer(new RecyclerViewManager.ItemTransformer<EventsInvitation>() {
                    @Override
                    public AbstractFlexibleItem createItem(EventsInvitation invitation) {
                        return new SubUserItem("INVITATION-" + String.valueOf(invitation.getRemoteId()), invitation.getUser(), mExpandableInviteHeader);
                    }
                })
                .setClearOnRefresh(true)
                //.setMinDelayForceRefresh(MIN_DELAY_FORCE_REFRESH_INVITE)
                .setCallback(new CursorPaginateDataLoader.Callback<EventsInvitation>() {

                    @Override
                    public void onLoadEnd(CursorPaginateDataLoader.LoadInfo<EventsInvitation> data, CursorPaginateDataLoader.LoadType type, boolean overwrite) {
                        mPlaceUsersAdapter.expand(mExpandableInviteHeader);
                        if (data != null && data.total != -1) mExpandableInviteHeader.setCount(data.total, mPlaceUsersAdapter);
                        notifyLoadsEnd(type);
                    }

                    @Override
                    public void onLoadError(Throwable error, CursorPaginateDataLoader.LoadType type) {
                        notifyLoadsEnd(type);
                    }

                    @Override
                    public void onLoadStart(CursorPaginateDataLoader.LoadType type) {}

                });
                //.setSwipeRefreshLayout(mSwipeRefreshLayout)
                //.enableEndlessScroll()

    }


    /**
     *
     */
    /*
    private void loadPeopleStats(){
        if (mExpandableHereHeader != null || mExpandableComingHeader != null){
            mSwipeLayout.setRefreshing(true);

            RestClient.buildCall(RestClient.service().eventPeopleStats(getEvent().getRemoteId()))
                    .onResponse(new HttpCallback<EventPeopleStats>() {
                        @Override
                        public void successful(EventPeopleStats peopleStat) {
                            // TODO [optimize] store in local
                        }
                    })
                    .onError(new NetworkErrorCallback(getContext()))
                    .perform();
        }
    }*/

    @SuppressWarnings({"ConstantConditions", "NullableProblems"})
    private void initializeRecyclerView(Bundle savedInstanceState) {
        //Experimenting NEW features (v5.0.0)
        mPlaceUsersAdapter.setAnimationOnScrolling(false);
        mPlaceUsersAdapter.setAnimationOnReverseScrolling(true);
        mPlaceUsersAdapter.setAutoCollapseOnExpand(false);
        mPlaceUsersAdapter.setAutoScrollOnExpand(false);
        mPlaceUsersAdapter.setRemoveOrphanHeaders(false);

        mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true); //Size of RV will not change
        mRecyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
                //NOTE: This allows to receive Payload objects when notifyItemChanged is called by the Adapter!!!
                return true;
            }
        });

        mRecyclerView.setAdapter(mPlaceUsersAdapter);
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
    }

    public void notifyLoadsEnd(CursorPaginateDataLoader.LoadType type){
        int value = loadCounter.decrementAndGet();
        if (value <= 0){
            mSwipeLayout.setRefreshing(false);
        }
    }

    @Override
    public void onTabSelected() {
        if(mRecyclerView!=null ) {
            mRecyclerView.smoothScrollToPosition(0);
        }
        if (loadCounter != null) return;
        this.onRefresh();
    }


    @Override
    public void onTabUnselected() {

    }

    @Override
    public void onRefresh() {
        loadCounter = new AtomicInteger(0);
        mSwipeLayout.setRefreshing(true);
        if (mExpandableHereHeader != null) {
            loadCounter.incrementAndGet();
            hereManager.load();
        }
        if (mExpandableComingHeader != null) {
            loadCounter.incrementAndGet();
            comingManager.load();
        }
        if (mExpandableInviteHeader != null){
            loadCounter.incrementAndGet();
            inviteSentManager.load();
        }
    }

}
