package com.timappweb.timapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.timappweb.timapp.adapters.flexibleadataper.UserItem;
import com.timappweb.timapp.config.IntentsUtils;
import com.timappweb.timapp.data.loader.MultipleEntryLoaderCallback;
import com.timappweb.timapp.data.models.Event;
import com.timappweb.timapp.data.models.EventsInvitation;
import com.timappweb.timapp.data.models.EventPost;
import com.timappweb.timapp.data.models.UserEvent;
import com.timappweb.timapp.rest.callbacks.HttpCallback;
import com.timappweb.timapp.rest.RestClient;
import com.timappweb.timapp.sync.DataSyncAdapter;

import java.util.LinkedList;
import java.util.List;

import eu.davidea.flexibleadapter.common.DividerItemDecoration;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flipview.FlipView;
import retrofit2.Call;


public class EventPeopleFragment extends EventBaseFragment {

    private static final String TAG = "EventTagsFragment";
    private static final long MAX_UPDATE_DELAY = 3600 * 1000;

    private Context         context;

    private MyFlexibleAdapter mPlaceUsersAdapter;

    private View            progressView;
    private View            noPostsView;
    private View            noConnectionView;
    private SwipeRefreshLayout mSwipeLayout;
    private FloatingActionButton postButton;
    private RecyclerView mRecyclerView;

    private ExpandableHeaderItem mExpandableHere;
    private ExpandableHeaderItem mExpandableComing;
    private ExpandableHeaderItem mExpandableInvite;
    private LinkedList mItems;

    //private RecyclerViewMaterialAdapter mAdapter;

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

        // viewContainer = (ObservableScrollView) root.findViewById(R.id.scrollView);
        progressView = view.findViewById(R.id.progress_view);
        noPostsView = view.findViewById(R.id.no_posts_view);
        noConnectionView = view.findViewById(R.id.no_connection_view);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_place_people);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_people);
        //postButton = (FloatingActionButton) view.findViewById(R.id.post_button);

        getLoaderManager().initLoader(EventActivity.LOADER_ID_USERS, null, new UserStatusLoader(this.getContext(), eventActivity.getEvent()));
        if (MyApplication.isLoggedIn()){
            getLoaderManager().initLoader(EventActivity.LOADER_ID_INVITATIONS, null, new InviteSentLoader(this.getContext(), eventActivity.getEvent()));
        }

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().getLoader(EventActivity.LOADER_ID_USERS).forceLoad();
                if (MyApplication.isLoggedIn()) {
                    getLoaderManager().getLoader(EventActivity.LOADER_ID_INVITATIONS).forceLoad();
                }
            }
        });
    }

    private void loadPosts() {
        Call<List<EventPost>> call = RestClient.service().viewPostsForPlace(eventActivity.getEventId());
        RestClient.buildCall(call)
                .onResponse(new HttpCallback<List<EventPost>>() {
                    @Override
                    public void successful(List<EventPost> list) {
                        //mPlaceUsersAdapter.addData(UserPlaceStatusEnum.HERE, list);
                    }

                    @Override
                    public void notSuccessful() {
                        noConnectionView.setVisibility(View.VISIBLE);
                    }
                })
                .perform();
    }


    @Override
    public void onResume() {
        Log.v(TAG, "onResume()");
        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Settings for FlipView
        FlipView.resetLayoutAnimationDelay(true, 1000L);

        //Create New Database and Initialize RecyclerView
        mItems = new LinkedList();
        mItems.add(new PlaceHolderItem("PLACEHOLDER0"));
        mExpandableHere = new ExpandableHeaderItem("HERE", context.getResources().getString(R.string.header_here));
        mItems.add(mExpandableHere);

        mExpandableComing = new ExpandableHeaderItem("COMING", context.getResources().getString(R.string.header_coming));
        mItems.add(mExpandableComing);

        mExpandableInvite = new ExpandableHeaderItem("INVITE", context.getResources().getString(R.string.header_invited));
        mItems.add(mExpandableInvite);

        initializeRecyclerView(savedInstanceState);

        //Settings for FlipView
        FlipView.stopLayoutAnimation();
    }

    @SuppressWarnings({"ConstantConditions", "NullableProblems"})
    private void initializeRecyclerView(Bundle savedInstanceState) {

        //List<AbstractFlexibleItem> list = new LinkedList();
        //list.add(new PlaceHolderItem("PLACEHOLDER0"));

        mPlaceUsersAdapter = new MyFlexibleAdapter(mItems, getActivity());
        //Experimenting NEW features (v5.0.0)
        mPlaceUsersAdapter.setAnimationOnScrolling(true);
        mPlaceUsersAdapter.setAnimationOnReverseScrolling(true);
        mPlaceUsersAdapter.setAutoCollapseOnExpand(false);
        mPlaceUsersAdapter.setAutoScrollOnExpand(false);
        mPlaceUsersAdapter.setRemoveOrphanHeaders(false);

        mRecyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mPlaceUsersAdapter);
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

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout_place_people);
        //mListener.onFragmentChange(swipeRefreshLayout, mRecyclerView, SelectableAdapter.MODE_IDLE);

        //mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        //mAdapter = new RecyclerViewMaterialAdapter(mPlaceUsersAdapter);
        mRecyclerView.setAdapter(mPlaceUsersAdapter);
        //mPlaceUsersAdapter.onAttachedToRecyclerView(mRecyclerView);
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView, null);
    }

    // =============================================================================================

    class UserStatusLoader extends MultipleEntryLoaderCallback<UserEvent> {

        public UserStatusLoader(Context context, Event event) {
            super(context, MAX_UPDATE_DELAY, DataSyncAdapter.SYNC_TYPE_EVENT_USERS, UserEvent.queryForPlace(event));
            this.syncOption.getBundle().putLong(DataSyncAdapter.SYNC_PARAM_EVENT_ID, event.getRemoteId());
            this.setSwipeAndRefreshLayout(mSwipeLayout, false);
        }

        @Override
        public void onLoadFinished(Loader<List<UserEvent>> loader, List<UserEvent> data) {
            super.onLoadFinished(loader, data);
            int herePosition = mItems.indexOf(mExpandableHere);
            int comingPosition = mItems.indexOf(mExpandableComing);

            mExpandableComing.removeSubItems();
            mExpandableHere.removeSubItems();
            // TODO clear
            for (UserEvent userEvent: data){
                UserItem item = new UserItem(userEvent.status.toString()+"-" + String.valueOf(userEvent.getRemoteId()), userEvent.getUser());
                switch (userEvent.status){
                    case COMING:
                        item.setHeader(mExpandableComing);
                        mExpandableComing.addSubItem(item);
                        break;
                    case HERE:
                        item.setHeader(mExpandableHere);
                        mExpandableHere.addSubItem(item);
                        break;
                    default:
                        continue;
                }
            }

            mExpandableHere.setExpanded(false);
            mExpandableComing.setExpanded(false);
            mPlaceUsersAdapter.notifyItemChanged(herePosition);
            mPlaceUsersAdapter.notifyItemChanged(comingPosition);
            mPlaceUsersAdapter.expand(herePosition);
            mPlaceUsersAdapter.expand(comingPosition);
        }

    }

    class InviteSentLoader extends MultipleEntryLoaderCallback<EventsInvitation> {

        public InviteSentLoader(Context context, Event event) {
            super(context, MAX_UPDATE_DELAY,
                    DataSyncAdapter.SYNC_TYPE_EVENT_INVITED,
                    MyApplication.getCurrentUser().getInviteSentQuery(event.getId()));

            this.syncOption.getBundle().putLong(DataSyncAdapter.SYNC_PARAM_EVENT_ID, event.getRemoteId());
            this.setSwipeAndRefreshLayout(mSwipeLayout, false);
        }

        @Override
        public void onLoadFinished(Loader<List<EventsInvitation>> loader, List<EventsInvitation> data) {
            super.onLoadFinished(loader, data);
            int position = mItems.indexOf(mExpandableInvite);

            mExpandableInvite.removeSubItems();
            for (EventsInvitation invitation: data){
                UserItem item = new UserItem("INVITATION-" + String.valueOf(invitation.getRemoteId()), invitation.getUser(), mExpandableInvite);
                mExpandableInvite.addSubItem(item);
            }
            mExpandableInvite.setExpanded(false);
            mPlaceUsersAdapter.notifyItemChanged(position);
            mPlaceUsersAdapter.expand(position);
        }

    }
}
