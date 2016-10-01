package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.timappweb.timapp.MyApplication;
import com.timappweb.timapp.R;
import com.timappweb.timapp.activities.EventActivity;
import com.timappweb.timapp.rest.callbacks.NetworkErrorCallback;
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.PlaceHolderItem;
import com.timappweb.timapp.adapters.flexibleadataper.models.PeopleHeaderItem;
import com.timappweb.timapp.adapters.flexibleadataper.models.SubUserItem;
import com.timappweb.timapp.data.loader.SyncDataLoader;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventPeopleStats;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.listeners.OnTabSelectedListener;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.sync.SyncAdapterOption;
import com.timappweb.timapp.sync.data.DataSyncAdapter;
import com.timappweb.timapp.utils.loaders.AutoModelLoader;

import java.util.List;

import eu.davidea.flexibleadapter.common.DividerItemDecoration;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flipview.FlipView;
import com.timappweb.timapp.views.SwipeRefreshLayout;

import org.greenrobot.eventbus.EventBus;


public class EventPeopleFragment extends EventBaseFragment implements OnTabSelectedListener, android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener {

    private static final String     TAG                             = "EventTagsFragment";
    private static final long       MAX_UPDATE_DELAY                = 3600 * 1000;

    // ---------------------------------------------------------------------------------------------

    private Context                 context;
    private MyFlexibleAdapter       mPlaceUsersAdapter;
    private SwipeRefreshLayout mSwipeLayout;
    private FloatingActionButton    postButton;
    private RecyclerView            mRecyclerView;
    private PeopleHeaderItem    mExpandableHereHeader;
    private PeopleHeaderItem    mExpandableComingHeader;
    private PeopleHeaderItem    mExpandableInviteHeader;
    private Loader<List<EventsInvitation>> mInviteLoader;
    private UserStatusLoader userStatusLoader;
    private InviteSentLoader inviteSentLoader;
    private Loader<List<UserEvent>> mUserStatusLoader;

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
        context = eventActivity.getBaseContext();

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_place_people);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_people);

        mSwipeLayout.setOnRefreshListener(this);

        initUserStatusLoader();
        if (MyApplication.isLoggedIn()){
            initInviteSentLoader();
        }
    }

    private void initUserStatusLoader() {
        userStatusLoader = new UserStatusLoader(this.getContext(), eventActivity.getEvent())
                .setModelLoader(new AutoModelLoader<UserEvent>(getContext(),
                        UserEvent.class,
                        getEvent().getPeopleQuery(),
                        false))
                .setSyncOptions(new SyncAdapterOption().setType(DataSyncAdapter.SYNC_TYPE_EVENT_USERS).setLong(DataSyncAdapter.SYNC_PARAM_EVENT_ID, getEvent().getRemoteId()))
                .setSwipeAndRefreshLayout(mSwipeLayout, false)
                .setCallback(new SyncDataLoader.Callback<UserEvent>() {
                    @Override
                    public void onLoadEnd(List<UserEvent> data) {
                        mPlaceUsersAdapter.removeItems(mExpandableComingHeader);
                        mPlaceUsersAdapter.removeItems(mExpandableHereHeader);
                        for (UserEvent userEvent: data){
                            SubUserItem item = new SubUserItem(userEvent.status.toString()+ "-" + String.valueOf(userEvent.getRemoteId()), userEvent.getUser());
                            switch (userEvent.status){
                                case COMING:
                                    mPlaceUsersAdapter.addSubItem(mExpandableComingHeader, item);
                                    break;
                                case HERE:
                                    mPlaceUsersAdapter.addSubItem(mExpandableHereHeader, item);
                                    break;
                                default:
                                    continue;
                            }
                        }
                        mPlaceUsersAdapter.expand(mExpandableHereHeader);
                        mPlaceUsersAdapter.expand(mExpandableComingHeader);
                        EventPeopleFragment.this.loadPeopleStats();
                    }

                    @Override
                    public void onLoadError(Throwable error) {
                        // TODO
                        Log.e(TAG, "Load error: " + error.getMessage());
                    }
                });
    }

    private void initInviteSentLoader() {
        inviteSentLoader = new InviteSentLoader(this.getContext())
                .setModelLoader(new AutoModelLoader<>(getContext(),
                        EventsInvitation.class,
                        MyApplication.getCurrentUser().getInviteSentQuery(getEvent().getId()),
                        false))
                .setSwipeAndRefreshLayout(mSwipeLayout, false)
                .setSyncOptions(new SyncAdapterOption().setType(DataSyncAdapter.SYNC_TYPE_INVITE_SENT).setLong(DataSyncAdapter.SYNC_PARAM_EVENT_ID, getEvent().getRemoteId()))
                .setCallback(new SyncDataLoader.Callback<EventsInvitation>() {
                    @Override
                    public void onLoadEnd(List<EventsInvitation> data) {
                        mPlaceUsersAdapter.removeItems(mExpandableInviteHeader);
                        for (EventsInvitation invitation: data){
                            SubUserItem item = new SubUserItem("INVITATION-" + String.valueOf(invitation.getRemoteId()), invitation.getUser(), mExpandableInviteHeader);
                            mPlaceUsersAdapter.addSubItem(mExpandableInviteHeader, item);
                        }
                        mPlaceUsersAdapter.expand(mExpandableInviteHeader);
                    }

                    @Override
                    public void onLoadError(Throwable error) {
                        // TODO
                        Log.e(TAG, "Load error: " + error.getMessage());
                    }
                });
    }


    /**
     *
     */
    private void loadPeopleStats(){
        RestClient.buildCall(RestClient.service().eventPeopleStats(getEvent().getRemoteId()))
            .onResponse(new HttpCallback<EventPeopleStats>() {
                @Override
                public void successful(EventPeopleStats peopleStat) {
                    mExpandableHereHeader.setCount(peopleStat.here, mPlaceUsersAdapter);
                    mExpandableComingHeader.setCount(peopleStat.coming, mPlaceUsersAdapter);
                    // TODO store in local
                }
            })
            .onError(new NetworkErrorCallback(getContext()))
            .perform();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (userStatusLoader != null) EventBus.getDefault().register(userStatusLoader);
        if (inviteSentLoader != null) EventBus.getDefault().register(inviteSentLoader);
    }

    @Override
    public void onStop() {
        if (userStatusLoader != null) EventBus.getDefault().unregister(userStatusLoader);
        if (inviteSentLoader != null) EventBus.getDefault().unregister(inviteSentLoader);
        super.onStop();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Settings for FlipView
        FlipView.resetLayoutAnimationDelay(true, 1000L); // TODO cst

        mPlaceUsersAdapter = new MyFlexibleAdapter(getActivity());
        mPlaceUsersAdapter.setPermanentDelete(true);

        mExpandableInviteHeader = new PeopleHeaderItem("INVITE", context.getResources().getString(R.string.header_invited));
        mPlaceUsersAdapter.addSection(mExpandableInviteHeader);

        mExpandableComingHeader = new PeopleHeaderItem("COMING", context.getResources().getString(R.string.header_coming));
        mPlaceUsersAdapter.addSection(mExpandableComingHeader);

        mExpandableHereHeader = new PeopleHeaderItem("HERE", context.getResources().getString(R.string.header_here));
        mPlaceUsersAdapter.addSection(mExpandableHereHeader);

        mPlaceUsersAdapter.addItem(0, new PlaceHolderItem("PLACEHOLDER0"));


        initializeRecyclerView(savedInstanceState);

        //Settings for FlipView
        FlipView.stopLayoutAnimation();
    }

    @SuppressWarnings({"ConstantConditions", "NullableProblems"})
    private void initializeRecyclerView(Bundle savedInstanceState) {

        //List<AbstractFlexibleItem> list = new LinkedList();

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
        //mRecyclerView.setItemAnimator(new SlideInRightAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                R.drawable.divider, 0));//Increase to add gap between sections (Works only with LinearLayout!)

        mRecyclerView.setAdapter(mPlaceUsersAdapter);
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
    }

    @Override
    public void onTabSelected() {
        if(mRecyclerView!=null) {
            mRecyclerView.smoothScrollToPosition(0);
        }
        loadPeopleStatusIfNeeded();
        if (MyApplication.isLoggedIn()) {
            loadInviteSentIfNeeded();
        }
    }

    private void loadPeopleStatusIfNeeded() {
        if (mUserStatusLoader != null) return;
        Log.d(TAG, "Loading people");
        mUserStatusLoader = getLoaderManager().initLoader(EventActivity.LOADER_ID_USERS, null, userStatusLoader);
    }

    private void loadInviteSentIfNeeded() {
        if (mInviteLoader != null) return;
        Log.d(TAG, "Loading invite sent by user");
        mInviteLoader = getLoaderManager().initLoader(EventActivity.LOADER_ID_INVITATIONS, null, inviteSentLoader);
    }

    @Override
    public void onRefresh() {
        if (userStatusLoader != null) {
            userStatusLoader.refresh();
        }
        if (inviteSentLoader != null) {
            inviteSentLoader.refresh();
        }
    }


    // =============================================================================================

    class UserStatusLoader extends SyncDataLoader<UserEvent, UserStatusLoader> {

        public UserStatusLoader(Context context, Event event) {
            super(context);
        }

    }

    class InviteSentLoader extends SyncDataLoader<EventsInvitation, InviteSentLoader> {
        public InviteSentLoader(Context context) {
            super(context);
        }
    }

}
