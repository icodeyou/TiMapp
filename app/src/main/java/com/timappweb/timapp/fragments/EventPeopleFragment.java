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
import com.timappweb.timapp.adapters.flexibleadataper.MyFlexibleAdapter;
import com.timappweb.timapp.adapters.flexibleadataper.ExpandableHeaderItem;
import com.timappweb.timapp.adapters.flexibleadataper.PlaceHolderItem;
import com.timappweb.timapp.adapters.flexibleadataper.models.SubUserItem;
import com.timappweb.timapp.data.loader.SyncDataLoader;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.listeners.OnTabSelectedListener;
import com.timappweb.timapp.utils.loaders.AutoModelLoader;

import java.util.List;

import eu.davidea.flexibleadapter.common.DividerItemDecoration;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flipview.FlipView;
import com.timappweb.timapp.views.SwipeRefreshLayout;


public class EventPeopleFragment extends EventBaseFragment implements OnTabSelectedListener {

    private static final String     TAG                             = "EventTagsFragment";
    private static final long       MAX_UPDATE_DELAY                = 3600 * 1000;

    // ---------------------------------------------------------------------------------------------

    private Context                 context;
    private MyFlexibleAdapter       mPlaceUsersAdapter;
    private View                    progressView;
    private View                    noPostsView;
    private View                    noConnectionView;
    private SwipeRefreshLayout mSwipeLayout;
    private FloatingActionButton    postButton;
    private RecyclerView            mRecyclerView;
    private ExpandableHeaderItem    mExpandableHereHeader;
    private ExpandableHeaderItem    mExpandableComingHeader;
    private ExpandableHeaderItem    mExpandableInviteHeader;
    private Loader<List<EventsInvitation>> mInviteLoader;
    private UserStatusLoader userStatusLoader;
    private InviteSentLoader inviteSentLoader;

    //private RecyclerViewMaterialAdapter mAdapter;

    // ---------------------------------------------------------------------------------------------

    public static EventPeopleFragment newInstance(int columnCount) {
        EventPeopleFragment fragment = new EventPeopleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventPeopleFragment() {}


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

        progressView = view.findViewById(R.id.progress_view);
        noPostsView = view.findViewById(R.id.no_posts_view);
        noConnectionView = view.findViewById(R.id.no_connection_view);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_place_people);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_people);

        initUserStatusLoader();


        //new DynamicListLoader(this, mPlaceUsersAdapter)
        getLoaderManager().initLoader(EventActivity.LOADER_ID_USERS, null, userStatusLoader);
        if (MyApplication.isLoggedIn()){
            initInviteSentLoader();
            mInviteLoader = getLoaderManager().initLoader(EventActivity.LOADER_ID_INVITATIONS, null, inviteSentLoader);
        }

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().getLoader(EventActivity.LOADER_ID_USERS).forceLoad();
                if (MyApplication.isLoggedIn()) {
                    mInviteLoader.forceLoad();
                }
            }
        });
    }

    private void initUserStatusLoader() {
        userStatusLoader = new UserStatusLoader(this.getContext(), eventActivity.getEvent())
                .setModelLoader(new AutoModelLoader<UserEvent>(getContext(),
                        UserEvent.class,
                        getEvent().getPeopleQuery(),
                        false))
                .setCallback(new SyncDataLoader.Callback<UserEvent>() {
                    @Override
                    public void onLoadEnd(List<UserEvent> data) {
                        mPlaceUsersAdapter.removeItems(mExpandableComingHeader);
                        mPlaceUsersAdapter.removeItems(mExpandableHereHeader);
                        for (UserEvent userEvent: data){
                            SubUserItem item = new SubUserItem(userEvent.status.toString()+"-" + String.valueOf(userEvent.getRemoteId()), userEvent.getUser());
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
                    }

                    @Override
                    public void onLoadError(Throwable error) {

                    }
                });
    }

    private void initInviteSentLoader() {
        inviteSentLoader = new InviteSentLoader(this.getContext())
                .setModelLoader(new AutoModelLoader<>(getContext(),
                        EventsInvitation.class,
                        MyApplication.getCurrentUser().getInviteSentQuery(getEvent().getId()),
                        false))
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

                    }
                });
    }

    @Override
    public void onResume() {
        Log.v(TAG, "onResume()");
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mInviteLoader!= null){
            mInviteLoader.forceLoad();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Settings for FlipView
        FlipView.resetLayoutAnimationDelay(true, 1000L);

        mPlaceUsersAdapter = new MyFlexibleAdapter(getActivity());
        mPlaceUsersAdapter.setPermanentDelete(true);

        mExpandableInviteHeader = new ExpandableHeaderItem("INVITE", context.getResources().getString(R.string.header_invited));
        mPlaceUsersAdapter.addSection(mExpandableInviteHeader);

        mExpandableComingHeader = new ExpandableHeaderItem("COMING", context.getResources().getString(R.string.header_coming));
        mPlaceUsersAdapter.addSection(mExpandableComingHeader);

        mExpandableHereHeader = new ExpandableHeaderItem("HERE", context.getResources().getString(R.string.header_here));
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

        //Add FastScroll to the RecyclerView, after the Adapter has been attached the RecyclerView!!!
        //mPlaceUsersAdapter.setFastScroller((FastScroller) getActivity().findViewById(R.id.fast_scroller),
        //        Utils.getColorAccent(getActivity()), (MainActivity) getActivity());

        //Experimenting NEW features (v5.0.0)
        //mPlaceUsersAdapter.setLongPressDragEnabled(true);//Enable long press to drag items

        //Show Headers at startUp! (not necessary if Headers are also Expandable)
        //mAdapter.setDisplayHeadersAtStartUp(true);
        //Add sample item on the top (not belongs to the library)
        //mPlaceUsersAdapter.addUserLearnedSelection(savedInstanceState == null);

        //SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout_place_people);
        //mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_IDLE);

        mRecyclerView.setAdapter(mPlaceUsersAdapter);
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
    }

    @Override
    public void onTabSelected() {
        mRecyclerView.smoothScrollToPosition(0);
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
